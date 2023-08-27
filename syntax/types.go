package syntax

import (
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/container"
	"github.com/inspirer/textmapper/util/graph"
	"github.com/inspirer/textmapper/util/ident"
	"github.com/inspirer/textmapper/util/set"
)

// Types describes the structure of the resulting AST.
type Types struct {
	RangeTypes []RangeType
	Categories []Category // sorted by name
}

// RangeToken maps a terminal to an AST node.
type RangeToken struct {
	Token  int
	Name   string
	Flags  []string
	Origin status.SourceNode
}

func (t RangeToken) String() string {
	if t.Name == "" {
		return "<unmapped>"
	}
	if len(t.Flags) == 0 {
		return t.Name
	}
	return t.Name + "/" + strings.Join(t.Flags, ",")
}

// Category describes a class of AST nodes that can be treated uniformly.
// E.g. statements or expressions.
type Category struct {
	Name  string
	Types []string
}

func (c Category) String() string {
	var sb strings.Builder
	sb.WriteString(c.Name)
	sb.WriteByte(':')
	for _, t := range c.Types {
		sb.WriteByte(' ')
		sb.WriteString(t)
	}
	return sb.String()
}

// RangeType describes an AST node type.
type RangeType struct {
	Name   string
	Fields []RangeField
}

func (t *RangeType) DecodeField(i int) []RangeField {
	var ret []RangeField
	for i >= 0 {
		field := t.Fields[i]
		ret = append(ret, field)
		i = field.FetchAfter
	}
	for i, j := 0, len(ret)-1; i < j; i, j = i+1, j-1 {
		ret[i], ret[j] = ret[j], ret[i]
	}
	return ret
}

// Descriptor returns a short descriptor of the field.
func (t *RangeType) Descriptor() string {
	var sb strings.Builder
	for _, f := range t.Fields {
		if sb.Len() > 0 {
			sb.WriteByte(' ')
		}
		f := field{name: f.Name, types: f.Selector, isList: f.IsList, nullable: !f.IsRequired}
		sb.WriteString(f.String())
	}
	return sb.String()
}

// RangeField describes an AST field.
type RangeField struct {
	Name       string
	Selector   []string // lists nested range types and categories, sorted
	FetchAfter int      // when non-negative, this field should be fetched as a sibling of another field
	IsRequired bool     // present in all ASTs
	IsList     bool
	Origin     status.SourceNode
}

// ExtraType gets injected into the grammar without any analysis.
type ExtraType struct {
	Name       string
	Implements []string
	Origin     status.SourceNode
}

// TypeOptions parameterizes the AST mapping logic.
type TypeOptions struct {
	EventFields bool
	GenSelector bool
	ExtraTypes  []ExtraType
}

// ExtractTypes analyzes all the arrows in the grammar and comes up with a
// structure of the produced AST.
//
// Note: the function is expected to work on a grammar without templates.
func ExtractTypes(m *Model, tokens []RangeToken, opts TypeOptions) (*Types, error) {
	c := newTypeCollector(m, tokens, opts)
	c.resolveTypes()
	switch {
	case opts.EventFields:
		c.resolveFields()
		c.resolveCategories()
		c.fixConflictingFields()
	case opts.GenSelector:
		c.resolveCategories()
	}
	return c.out, c.s.Err()
}

func newTypeCollector(m *Model, tokens []RangeToken, opts TypeOptions) *typeCollector {
	seen := make(map[int]bool) // nonterminals
	var nonterms []int

	var dfs func(expr *Expr)
	dfs = func(expr *Expr) {
		switch expr.Kind {
		case Reference:
			if nonterm := expr.Symbol - len(m.Terminals); nonterm >= 0 && !seen[nonterm] {
				seen[nonterm] = true
				nonterms = append(nonterms, nonterm)
				dfs(m.Nonterms[nonterm].Value)
			}
		case Lookahead, LookaheadNot:
			return
		case Conditional:
			log.Fatalf("%v is not properly instantiated", expr)
		}
		for _, e := range expr.Sub {
			dfs(e)
		}
	}

	for _, inp := range m.Inputs {
		if !inp.Synthetic {
			seen[inp.Nonterm] = true
			nonterms = append(nonterms, inp.Nonterm)
			dfs(m.Nonterms[inp.Nonterm].Value)
		}
	}
	sort.Ints(nonterms)

	reportTokens := container.NewBitSet(len(m.Terminals))
	for _, tok := range tokens {
		reportTokens.Set(tok.Token)
	}
	tokenSet := container.NewBitSet(len(m.Terminals))

	var arrows []*Expr
	dfs = func(expr *Expr) {
		switch expr.Kind {
		case Arrow:
			arrows = append(arrows, expr)
		case Lookahead, LookaheadNot:
			return
		case Reference:
			if expr.Symbol < len(m.Terminals) && reportTokens.Get(expr.Symbol) {
				tokenSet.Set(expr.Symbol)
			}
		}
		for _, e := range expr.Sub {
			dfs(e)
		}
	}
	for _, nt := range nonterms {
		dfs(m.Nonterms[nt].Value)
	}

	isCat := make(map[string]bool)
	for _, cat := range m.Cats {
		isCat[cat] = true
	}

	return &typeCollector{
		m:            m,
		opts:         opts,
		nonterms:     nonterms,
		arrows:       arrows,
		isCat:        isCat,
		tokens:       tokens,
		reportTokens: reportTokens,
		tokenSet:     tokenSet.Slice(nil),
		out:          &Types{},
	}
}

type typeCollector struct {
	m            *Model
	opts         TypeOptions
	nonterms     []int
	arrows       []*Expr
	isCat        map[string]bool
	tokens       []RangeToken
	reportTokens container.BitSet
	tokenSet     []int // actually used "tokens", sorted

	types      map[string]int // name -> index in out.RangeTypes
	tokenTypes map[int]int
	cats       map[string][]int

	// Tarjan state for detecting AST cycles on nonterminals
	stack    []int
	index    []int
	lowLink  []int
	onStack  container.BitSet
	self     container.BitSet // list nonterminals (referencing self)
	curr     int
	referrer int
	cache    map[int]phrase // for nonterm, all connected nonterminals get the same phrase

	out *Types
	s   status.Status
}

func (c *typeCollector) nontermPhrase(nonterm int) phrase {
	if ret, ok := c.cache[nonterm]; ok {
		return ret
	}

	// The following implements the Tarjan algorithm squeezed into a single recursive function.
	if c.index[nonterm] >= 0 {
		if c.referrer >= 0 {
			if c.onStack.Get(nonterm) && c.index[nonterm] < c.lowLink[c.referrer] {
				// Note: if nonterm is not on the stack, it is already part of the SCC.
				c.lowLink[c.referrer] = c.index[nonterm]
			}
			if c.lowLink[nonterm] < c.lowLink[c.referrer] {
				c.lowLink[c.referrer] = c.lowLink[nonterm]
			}
			if c.referrer == nonterm {
				c.self.Set(nonterm)
			}
		}
		return phrase{}
	}

	base := len(c.stack)
	c.index[nonterm] = c.curr
	c.lowLink[nonterm] = c.curr
	c.curr++
	c.stack = append(c.stack, nonterm)
	c.onStack.Set(nonterm)

	// Potentially recurse into referenced nonterminals.
	old := c.referrer
	c.referrer = nonterm
	ret := c.exprPhrase(c.m.Nonterms[nonterm].Value)
	c.referrer = old

	if c.lowLink[nonterm] == c.index[nonterm] {
		if len(c.stack[base:]) > 1 || c.self.Get(nonterm) {
			// In case of cycles (or self reference), turn all fields into lists.
			ret.ordered = false
			for i, f := range ret.fields {
				clone := *f
				clone.isList = true
				ret.fields[i] = &clone
			}
		}
		for _, nt := range c.stack[base:] {
			c.cache[nt] = ret
			c.onStack.Clear(nt)
		}
		c.stack = c.stack[:base]
	}
	if c.referrer >= 0 && c.lowLink[nonterm] < c.lowLink[c.referrer] {
		c.lowLink[c.referrer] = c.lowLink[nonterm]
	}
	return ret
}

func (c *typeCollector) exprPhrase(expr *Expr) phrase {
	switch expr.Kind {
	case Arrow:
		if expr.Name == ignoreContent {
			// Empty phrase.
			break
		}
		return newPhrase(expr)
	case Sequence, Choice:
		var list []phrase
		for _, sub := range expr.Sub {
			list = append(list, c.exprPhrase(sub))
		}
		if expr.Kind == Sequence {
			return concatPhrases(list, expr)
		}
		return mergePhrases(list, expr)
	case Assign, Append:
		p := c.exprPhrase(expr.Sub[0])
		if len(p.fields) == 1 {
			clone := *p.fields[0]
			clone.name = expr.Name
			clone.origin = expr.Origin
			if expr.Kind == Append {
				// TODO report an error if already a list
				clone.isList = true
			}
			clone.updateIdentity()
			return phrase{
				fields:  []*field{&clone},
				ordered: true,
				origin:  expr,
			}
		}

		var fields []string
		for _, f := range p.fields {
			fields = append(fields, f.String())
		}
		c.s.Errorf(expr.Origin, "multiple fields found behind an assignment: %s", fields)
		return p
	case Reference:
		if nonterm := expr.Symbol - len(c.m.Terminals); nonterm >= 0 {
			return c.nontermPhrase(nonterm)
		}
		// Some terminals get injected into the AST directly.
		if c.reportTokens.Get(expr.Symbol) {
			rt := c.tokenTypes[expr.Symbol]
			return newTermPhrase(c.out.RangeTypes[rt].Name, expr)
		}
	case Prec:
		return c.exprPhrase(expr.Sub[0])
	case Optional, List:
		p := c.exprPhrase(expr.Sub[0])
		var out []*field
		for _, f := range p.fields {
			clone := *f
			if expr.Kind == Optional {
				clone.nullable = true
			} else {
				clone.isList = true
				clone.nullable = clone.nullable || expr.ListFlags&OneOrMore == 0
			}
			out = append(out, &clone)
		}
		return phrase{fields: out, ordered: p.ordered, origin: expr}
	case Empty, Lookahead, LookaheadNot, Command, Set, StateMarker:
		// These do not have fields.
	default:
		log.Fatalf("%v is not properly instantiated", expr)
	}
	return phrase{origin: expr, ordered: true}
}

func (c *typeCollector) initTarjan() {
	size := len(c.m.Nonterms)
	c.stack = c.stack[:0]
	c.index = make([]int, size)
	for i := range c.index {
		c.index[i] = -1
	}
	c.lowLink = make([]int, size)
	c.onStack = container.NewBitSet(size)
	c.self = container.NewBitSet(size)
	c.curr = 0
	c.referrer = -1
	c.cache = make(map[int]phrase)
}

const ignoreContent = "__ignoreContent"

func (c *typeCollector) resolveTypes() {
	var types []string
	for _, arrow := range c.arrows {
		if !c.isCat[arrow.Name] && arrow.Name != ignoreContent {
			types = append(types, arrow.Name)
		}
	}
	types = sortAndDedup(types)

	c.types = make(map[string]int) // name -> index in def/c.out.RangeTypes
	for _, t := range types {
		c.types[t] = len(c.out.RangeTypes)
		c.out.RangeTypes = append(c.out.RangeTypes, RangeType{Name: t})
	}
	c.tokenTypes = make(map[int]int)
	for _, tok := range c.tokens {
		index, ok := c.types[tok.Name]
		if !ok {
			index = len(c.out.RangeTypes)
			c.types[tok.Name] = index
			c.out.RangeTypes = append(c.out.RangeTypes, RangeType{Name: tok.Name})
		}
		c.tokenTypes[tok.Token] = index
	}
}

func (c *typeCollector) resolveFields() {
	def := make([][]*Expr, len(c.out.RangeTypes))
	for _, arrow := range c.arrows {
		if !c.isCat[arrow.Name] && arrow.Name != ignoreContent {
			index := c.types[arrow.Name]
			def[index] = append(def[index], arrow.Sub[0])
		}
	}
	c.initTarjan()
	for index, exprs := range def {
		if len(exprs) == 0 {
			// This is a reported token.
			continue
		}
		var phrases []phrase
		for _, e := range exprs {
			phrases = append(phrases, c.exprPhrase(e))
		}
		p := mergePhrases(phrases, nil)
		var fields []RangeField
		for i, f := range p.fields {
			out := RangeField{
				Name:       ident.Produce(f.name, ident.CamelLower),
				Selector:   f.types,
				IsRequired: !f.nullable,
				IsList:     f.isList,
				FetchAfter: -1,
				Origin:     f.origin,
			}
			if p.ordered {
				out.FetchAfter = i - 1
			}
			if out.Name == "" {
				out.Name = f.types[0]
			}
			fields = append(fields, out)
		}
		c.out.RangeTypes[index].Fields = fields
	}
}

type cardinality uint8

const (
	oneNode cardinality = 1 << iota
	nothing
	ambiguous
)

func (c *typeCollector) resolveCategories() {
	var cats []*set.FutureSet
	var def [][]*Expr
	byName := make(map[string]int) // name -> index in cats/c.out.Categories
	closure := set.NewClosure(len(c.out.RangeTypes))

	for _, arrow := range c.arrows {
		if !c.isCat[arrow.Name] || arrow.Name == ignoreContent {
			continue
		}
		index, ok := byName[arrow.Name]
		if !ok {
			index = len(cats)
			cats = append(cats, closure.Add(nil))
			def = append(def, nil)
			c.out.Categories = append(c.out.Categories, Category{Name: arrow.Name})
			byName[arrow.Name] = index
		}
		def[index] = append(def[index], arrow.Sub[0])
	}

	if _, ok := byName["TokenSet"]; !ok && len(c.tokenSet) > 0 {
		// Instantiate a synthetic category: TokenSet (unless the name is already taken).
		types := container.NewBitSet(len(c.out.RangeTypes))
		for _, t := range c.tokenSet {
			types.Set(c.tokenTypes[t])
		}
		cats = append(cats, closure.Add(types.Slice(nil)))
		c.out.Categories = append(c.out.Categories, Category{Name: "TokenSet"})
	}

	var target *set.FutureSet
	nonterms := make(map[int]*set.FutureSet)
	card := make(map[int]cardinality)
	cycle := make(map[int]bool) // nonterminals

	var dfs func(expr *Expr) cardinality
	dfs = func(expr *Expr) cardinality {
		switch expr.Kind {
		case Arrow:
			switch {
			case expr.Name == ignoreContent:
				return nothing
			case c.isCat[expr.Name]:
				target.Include(cats[byName[expr.Name]])
			default:
				target.Include(closure.Add([]int{c.types[expr.Name]}))
			}
			return oneNode
		case Sequence:
			ret := nothing
			var suppress bool
			for _, sub := range expr.Sub {
				v := dfs(sub)
				switch {
				case ret == oneNode && v == oneNode:
					ret = ambiguous
				case v == ambiguous:
					suppress = true
					ret = v
				case ret == nothing:
					ret = v
				}
			}
			if ret != oneNode && ret != nothing {
				if !suppress {
					c.s.Errorf(expr.Origin, "'%v' must produce exactly one node", expr.String())
				}
				ret = ambiguous
			}
			return ret
		case Choice:
			var ret cardinality
			for _, sub := range expr.Sub {
				ret |= dfs(sub)
			}
			if ret != oneNode && ret != nothing {
				if ret&ambiguous == 0 {
					c.s.Errorf(expr.Origin, "'%v' must produce exactly one node", expr.String())
				}
				ret = ambiguous
			}
			return ret
		case Reference:
			if nonterm := expr.Symbol - len(c.m.Terminals); nonterm >= 0 {
				set, ok := nonterms[nonterm]
				if !ok {
					set = closure.Add(nil)
					nonterms[nonterm] = set
				}
				target.Include(set)
				if ret, ok := card[nonterm]; ok {
					// Note: if ret says "ambiguous", we've reported this already.
					return ret
				}

				if cycle[nonterm] {
					// This will poison all the nonterminals that belong to the cycle.
					c.s.Errorf(expr.Origin, "'%v' is recursive and cannot be used inside a category expression", expr.String())
					return ambiguous
				}
				// Recurse into the nonterminal.
				old := target
				target = set
				cycle[nonterm] = true
				ret := dfs(c.m.Nonterms[nonterm].Value)
				delete(cycle, nonterm)
				target = old

				card[nonterm] = ret
				// Note: if ret says "ambiguous", we've reported this already.
				return ret
			}
			if c.reportTokens.Get(expr.Symbol) {
				target.Include(closure.Add([]int{c.tokenTypes[expr.Symbol]}))
			}
		case Prec, Assign, Append:
			return dfs(expr.Sub[0])
		case Optional, List:
			ret := dfs(expr.Sub[0])
			if ret != nothing {
				c.s.Errorf(expr.Origin, "'%v' cannot be used inside a category expression", expr.String())
				return ambiguous
			}
			return ret
		case Empty, Lookahead, LookaheadNot, Command, Set, StateMarker:
			// These do not have fields.
		default:
			log.Fatalf("%v is not properly instantiated", expr)
		}
		return nothing
	}
	for index, exprs := range def {
		target = cats[index]
		for _, e := range exprs {
			if dfs(e) != nothing {
				continue
			}
			if e.Kind == Arrow && e.Name == ignoreContent {
				continue
			}
			c.s.Errorf(e.Origin, "'%v' must produce exactly one node", e.String())
		}
	}

	closure.Compute()
	c.cats = make(map[string][]int)
	for i, set := range cats {
		c.cats[c.out.Categories[i].Name] = set.Set
		for _, t := range set.Set {
			c.out.Categories[i].Types = append(c.out.Categories[i].Types, c.out.RangeTypes[t].Name)
		}
	}

	// Append extra types at the very end.
	for _, t := range c.opts.ExtraTypes {
		for _, cat := range t.Implements {
			i, ok := byName[cat]
			if !ok {
				c.s.Errorf(t.Origin, "'%v' is not a valid category reference", cat)
				continue
			}
			c.out.Categories[i].Types = append(c.out.Categories[i].Types, t.Name)
		}
	}

	// Sort the result for deterministic output.
	for _, cat := range c.out.Categories {
		sort.Strings(cat.Types)
	}
	sort.Slice(c.out.Categories, func(i, j int) bool {
		return c.out.Categories[i].Name < c.out.Categories[j].Name
	})
}

func resolveTypes(selector []string, types map[string]int, categories map[string][]int) []int {
	var ret []int
	for _, t := range selector {
		if list, ok := categories[t]; ok {
			ret = append(ret, list...)
			continue
		}
		ret = append(ret, types[t])
	}
	sort.Ints(ret)
	list := ret
	ret = ret[:0]
	var prev int
	for i, val := range list {
		if i > 0 && prev == val {
			continue
		}
		ret = append(ret, val)
		prev = val
	}
	return ret
}

func (c *typeCollector) fixConflictingFields() {
	type resolution struct {
		hasDep      bool
		incomingDep int
	}
	var resolved []resolution

	for _, t := range c.out.RangeTypes {
		seen := make(map[int]int) // type -> field in "t"

		if len(t.Fields) <= 1 {
			continue
		}
		resolved = resolved[:0]
		ordered := t.Fields[1].FetchAfter == 0
		for i, field := range t.Fields {
			var hasDep bool
			for _, t := range resolveTypes(field.Selector, c.types, c.cats) {
				prev, ok := seen[t]
				seen[t] = i
				if ok {
					hasDep = true
					resolved[prev].incomingDep = i
				}
			}
			resolved = append(resolved, resolution{hasDep: hasDep})
		}
		prev := -1
		var suppress bool
		for i, field := range t.Fields {
			resolution := resolved[i]

			if ordered && (resolution.incomingDep != 0 || resolution.hasDep) {
				t.Fields[i].FetchAfter = prev
			} else {
				t.Fields[i].FetchAfter = -1
			}
			if resolution.incomingDep != 0 {
				other := t.Fields[resolution.incomingDep]
				if !suppress && (field.IsList || !field.IsRequired || !ordered) {
					// TODO fix the error origin
					c.s.Errorf(field.Origin, "fields %v and %v of %v contain overlapping sets of node types", field.Name, other.Name, t.Name)
					suppress = true
				}
				prev = i
			}
		}
	}
}

type field struct {
	name     string   // non-empty iff set explicitly
	types    []string // sorted; immutable; might contain categories
	isList   bool
	nullable bool
	identity string
	origin   status.SourceNode
}

func (f *field) updateIdentity() {
	if f.name != "" {
		f.identity = f.name
		return
	}
	var buf strings.Builder
	buf.WriteByte('=')
	for i, t := range f.types {
		if i > 0 {
			buf.WriteByte('|')
		}
		buf.WriteString(t)
	}
	f.identity = buf.String()
}

func mergeFields(fields ...*field) *field {
	ret := *fields[0]
	ret.types = append([]string(nil), ret.types...)
	for _, f := range fields[1:] {
		ret.isList = ret.isList || f.isList
		ret.nullable = ret.nullable || f.nullable
		ret.types = append(ret.types, f.types...)
		if f.name != ret.name {
			ret.name = ""
		}
		if ret.identity != f.identity {
			log.Fatalf("cannot merge fields: %v vs %v", ret.identity, f.identity)
		}
	}
	ret.types = sortAndDedup(ret.types)
	return &ret
}

func (f *field) String() string {
	needsName := f.name != "" && (len(f.types) != 1 || f.types[0] != f.name)
	var sb strings.Builder
	if needsName {
		sb.WriteString(f.name)
		sb.WriteByte('=')
	}
	parens := len(f.types) > 1 || f.isList
	if parens {
		sb.WriteByte('(')
	}
	for i, t := range f.types {
		if i > 0 {
			sb.WriteString(" | ")
		}
		sb.WriteString(t)
	}
	if parens {
		sb.WriteByte(')')
	}
	switch {
	case f.isList && f.nullable:
		sb.WriteByte('*')
	case f.isList:
		sb.WriteByte('+')
	case f.nullable:
		sb.WriteByte('?')
	}
	return sb.String()
}

type phrase struct {
	fields  []*field
	origin  *Expr
	ordered bool
}

func newPhrase(arrow *Expr) phrase {
	f := &field{
		types:  []string{arrow.Name},
		origin: arrow.Origin,
	}
	f.updateIdentity()
	return phrase{
		fields:  []*field{f},
		origin:  arrow,
		ordered: true,
	}
}

func newTermPhrase(name string, ref *Expr) phrase {
	f := &field{
		types:  []string{name},
		origin: ref.Origin,
	}
	f.updateIdentity()
	return phrase{
		fields:  []*field{f},
		origin:  ref,
		ordered: true,
	}
}

func concatPhrases(phrases []phrase, origin *Expr) phrase {
	ret := phrase{ordered: true, origin: origin}
	m := make(map[string]int) // -> ret.fields index
	for _, p := range phrases {
		ret.ordered = ret.ordered && p.ordered
		for _, f := range p.fields {
			if i, ok := m[f.identity]; ok {
				ret.fields[i] = mergeFields(ret.fields[i], f)
				ret.fields[i].isList = true
				if i != len(ret.fields)-1 {
					// Out of order duplicate.
					ret.ordered = false
				}
				continue
			}
			m[f.identity] = len(ret.fields)
			ret.fields = append(ret.fields, f)
		}
	}
	return ret
}

func mergePhrases(phrases []phrase, origin *Expr) phrase {
	if len(phrases) == 1 {
		return phrases[0]
	}
	ret := phrase{origin: origin, ordered: true}
	var list [][]*field
	var g [][]int
	m := make(map[string]int) // -> g/list index
	for _, phrase := range phrases {
		var prev int
		ret.ordered = ret.ordered && phrase.ordered
		for i, f := range phrase.fields {
			index, ok := m[f.identity]
			if !ok {
				index = len(list)
				m[f.identity] = index
				g = append(g, nil)
				list = append(list, []*field{f})
			} else {
				list[index] = append(list[index], f)
			}
			if i > 0 {
				g[index] = append(g[index], prev)
			}
			prev = index
		}
	}

	if ret.ordered {
		// All phrases are ordered, so we can try to merge them.
		if path := graph.LongestPath(g); len(path) == len(g) {
			for i := len(path) - 1; i >= 0; i-- {
				index := path[i]
				f := mergeFields(list[index]...)
				if len(list[index]) < len(phrases) {
					f.nullable = true
				}
				ret.fields = append(ret.fields, f)
			}
			return ret
		}
	}

	// There is no clear order between the fields.
	ret.ordered = false
	topoSort(list, g)
	for _, fields := range list {
		f := mergeFields(fields...)
		if len(fields) < len(phrases) {
			f.nullable = true
		}
		ret.fields = append(ret.fields, f)
	}
	return ret
}

func topoSort(list [][]*field, g [][]int) {
	height := make([]int, len(list))
	done := container.NewBitSet(len(list))
	var fn func(i int) int
	fn = func(i int) int {
		if done.Get(i) {
			return height[i]
		}
		done.Set(i)
		ret := 0
		for _, e := range g[i] {
			if val := fn(e) + 1; val > ret {
				ret = val
			}
		}
		height[i] = ret
		return ret
	}
	var max int
	for i := range list {
		if val := fn(i); val > max {
			max = val
		}
	}

	// Bucket sort.
	cnt := make([]int, max+1)
	for _, h := range height {
		cnt[h]++
	}
	var target int
	for i, val := range cnt {
		cnt[i] = target
		target += val
	}
	out := make([][]*field, len(list))
	for i, h := range height {
		e := cnt[h]
		cnt[h]++
		out[e] = list[i]
	}

	// Sort fields within a bucket.
	target = 0
	for _, max := range cnt {
		slice := out[target:max]
		target = max
		sort.Slice(slice, func(i, j int) bool {
			return slice[i][0].identity < slice[j][0].identity
		})
	}

	copy(list, out) // copy it back
}

func sortAndDedup(list []string) []string {
	sort.Strings(list)

	ret := list[:0]
	var prev string
	for i, s := range list {
		if i > 0 && prev == s {
			continue
		}
		ret = append(ret, s)
		prev = s
	}
	return ret
}
