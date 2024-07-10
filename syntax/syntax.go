// Package syntax analyzes and translates context-free grammars in the Textmapper notation into a
// simpler representation that is understood by a LALR parser generator.
package syntax

import (
	"fmt"
	"log"
	"strconv"
	"strings"

	"github.com/inspirer/textmapper/status"
)

// Model is a model of a language's syntax built on top of a set of terminals.
type Model struct {
	Terminals []string
	Params    []Param
	Nonterms  []*Nonterm // all params and nonterms must have distinct names
	Inputs    []Input
	Sets      []*TokenSet // token sets to compute
	Cats      []string    // categories
}

// Ref returns the string version of a symbol reference for debugging.
func (m *Model) Ref(sym int, args []Arg) string {
	if sym < len(m.Terminals) {
		return m.Terminals[sym]
	}
	nt := m.Nonterms[sym-len(m.Terminals)]
	if len(args) == 0 {
		return nt.Name
	}
	var list []string
	for _, arg := range args {
		var val string
		if arg.Value == "" {
			val = fmt.Sprintf("%v: %v", m.Params[arg.Param].Name, m.Params[arg.TakeFrom].Name)
		} else {
			val = fmt.Sprintf("%v: %q", m.Params[arg.Param].Name, arg.Value)
		}
		list = append(list, val)
	}
	return fmt.Sprintf("%v<%v>", nt.Name, strings.Join(list, ", "))
}

// ForEach visits all expressions of a given kind in the model.
func (m *Model) ForEach(kind ExprKind, consumer func(container *Nonterm, expr *Expr)) {
	seen := make(map[*Expr]bool)
	var visit func(nt *Nonterm, e *Expr)
	visit = func(nt *Nonterm, e *Expr) {
		if seen[e] {
			return
		}
		seen[e] = true
		if e.Kind == kind || kind == -1 {
			consumer(nt, e)
		}
		for _, sub := range e.Sub {
			visit(nt, sub)
		}
	}
	for _, nt := range m.Nonterms {
		visit(nt, nt.Value)
	}
}

// Rearrange reorders nonterminals using a given permutation.
func (m *Model) Rearrange(perm []int) {
	out := make([]*Nonterm, len(m.Nonterms))
	for i, nt := range m.Nonterms {
		if out[perm[i]] != nil {
			log.Fatal("permutation: invariant failure")
		}
		out[perm[i]] = nt
	}
	m.Nonterms = out

	// Update symbol references.
	terms := len(m.Terminals)
	m.ForEach(Reference, func(_ *Nonterm, expr *Expr) {
		if nt := expr.Symbol - terms; nt >= 0 {
			expr.Symbol = terms + perm[nt]
		}
	})
	for _, set := range m.Sets {
		set.ForEach(func(ts *TokenSet) {
			if nt := ts.Symbol - terms; nt >= 0 {
				ts.Symbol = terms + perm[nt]
			}
		})
	}
	for i, input := range m.Inputs {
		m.Inputs[i].Nonterm = perm[input.Nonterm]
	}
}

// Input introduces a start nonterminal.
type Input struct {
	Nonterm   int // Index in model.Nonterms
	NoEoi     bool
	Synthetic bool
}

// Param is a grammar-wide template parameter.
type Param struct {
	Name         string
	DefaultValue string
	Lookahead    bool
	Origin       status.SourceNode
}

// Nonterm is a grammar nonterminal.
type Nonterm struct {
	Name   string
	Type   string // the type of the associated value
	Params []int
	Value  *Expr // non-nil

	// TODO: support nonterminal inlining
	Inline bool // true for to-be-inlined nonterminals

	// When non-empty, this is a Lookahead nonterminal (implies Value is Empty). When two or more
	// lookahead nonterminals can be reduced in the same state, their lookahead predicates are
	// evaluated at runtime to determine which one should actually be reduced.
	LA     []LAPredicate
	Origin status.SourceNode

	// For use in syntax sugar expansions. Non-zero for instantiated nonterminals.
	group int
}

func (nt *Nonterm) ClearGroup() {
	nt.group = 0
}

func (nt *Nonterm) String() string {
	return nt.Name + ": " + nt.Value.String()
}

// LAPredicate is a restriction on the remaining input stream of tokens.
type LAPredicate struct {
	Input   int
	Negated bool
}

// Expr represents the right-hand side of a production rule (or its part).
type Expr struct {
	Kind       ExprKind
	Name       string
	Sub        []*Expr
	Symbol     int
	Args       []Arg
	Pos        int // Positional index of a reference, set, or list in the original rule.
	Predicate  *Predicate
	ListFlags  ListFlags
	ArrowFlags []string
	SetIndex   int
	CmdArgs    *CmdArgs
	Origin     status.SourceNode
	Model      *Model // Kept for some kinds for debugging. TODO error-prone, get rid of
}

// Equal returns true for equivalent grammar clauses.
func (e *Expr) Equal(oth *Expr) bool {
	if e.Kind != oth.Kind {
		return false
	}
	switch e.Kind {
	case Empty:
		return true
	case Reference:
		if len(e.Args) != len(oth.Args) {
			return false
		}
		for i, arg := range e.Args {
			if !arg.equal(oth.Args[i]) {
				return false
			}
		}
		return e.Symbol == oth.Symbol
	case Optional, LookaheadNot:
		return e.Sub[0].Equal(oth.Sub[0])
	case Choice, Sequence, Lookahead, List:
		if len(e.Sub) != len(oth.Sub) || e.ListFlags != oth.ListFlags {
			return false
		}
		for i, val := range e.Sub {
			if !val.Equal(oth.Sub[i]) {
				return false
			}
		}
		return true
	case Assign, Append:
		return e.Name == oth.Name && e.Sub[0].Equal(oth.Sub[0])
	case Arrow:
		return e.Name == oth.Name && sliceEqual(e.ArrowFlags, oth.ArrowFlags) && e.Sub[0].Equal(oth.Sub[0])
	case Prec:
		return e.Symbol == oth.Symbol && e.Sub[0].Equal(oth.Sub[0])
	case StateMarker, Command:
		return e.Name == oth.Name
	case Set:
		return e.SetIndex == oth.SetIndex
	case Conditional:
		return e.Predicate.equal(oth.Predicate) && e.Sub[0].Equal(oth.Sub[0])
	default:
		return false
	}
}

func (e *Expr) String() string {
	switch e.Kind {
	case Empty:
		return "%empty"
	case Optional:
		return fmt.Sprintf("%v?", parenthesize(e.Kind, e.Sub[0]))
	case Assign:
		return fmt.Sprintf("%v=%v", e.Name, parenthesize(e.Kind, e.Sub[0]))
	case Append:
		return fmt.Sprintf("%v+=%v", e.Name, parenthesize(e.Kind, e.Sub[0]))
	case Arrow:
		return fmt.Sprintf("%v -> %v", parenthesize(e.Kind, e.Sub[0]), e.Name)
	case Prec:
		var sym string
		if e.Model != nil {
			sym = e.Model.Terminals[e.Symbol]
		} else {
			sym = strconv.Itoa(e.Symbol)
		}
		return fmt.Sprintf("%v %%prec %v", parenthesize(e.Kind, e.Sub[0]), sym)
	case Conditional:
		return fmt.Sprintf("[%v] %v", e.Predicate.String(e.Model), e.Sub[0])
	case List:
		suffix := "*"
		if e.ListFlags&OneOrMore != 0 {
			suffix = "+"
		}
		if e.ListFlags&RightRecursive != 0 {
			suffix += "/rr"
		}
		var sep string
		if len(e.Sub) > 1 {
			sep = fmt.Sprintf(" separator %v", e.Sub[1])
		}
		return fmt.Sprintf("(%v%v)%v", e.Sub[0], sep, suffix)
	case Sequence:
		var buf strings.Builder
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		return buf.String()
	case Choice:
		var buf strings.Builder
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteString(" | ")
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		return buf.String()
	case Reference:
		if e.Model != nil {
			return e.Model.Ref(e.Symbol, e.Args)
		}
		if len(e.Args) == 0 {
			return strconv.Itoa(e.Symbol)
		}
		return fmt.Sprintf("%v<%v>", e.Symbol, e.Args)
	case Set:
		if e.Model != nil {
			return fmt.Sprintf("set(%v)", e.Model.Sets[e.SetIndex].String(e.Model))
		}
		return fmt.Sprintf("set(%v)", e.SetIndex)
	case StateMarker:
		return "." + e.Name
	case Command:
		return e.Name // includes {}
	case Lookahead:
		var buf strings.Builder
		buf.WriteString("(?= ")
		for i, sub := range e.Sub {
			if i > 0 {
				buf.WriteString(" & ")
			}
			buf.WriteString(parenthesize(e.Kind, sub))
		}
		buf.WriteString(")")
		return buf.String()
	case LookaheadNot:
		return fmt.Sprintf("!%v", e.Sub[0])
	default:
		log.Fatalf("cannot stringify kind=%v", e.Kind)
		return ""
	}
}

func parenthesize(outer ExprKind, sub *Expr) string {
	var paren bool
	switch sub.Kind {
	case Command, Lookahead, Set, Empty, List:
		// no parentheses
	case Choice:
		paren = true
	case Sequence:
		paren = outer != Arrow && outer != Prec && outer != Choice
	case Arrow:
		paren = outer != Arrow && outer != Choice
	default:
		paren = outer == Optional && sub.Kind != Reference
	}
	if paren {
		return fmt.Sprintf("(%v)", sub.String())
	}
	return sub.String()
}

// ListFlags define the layout of list production rules.
type ListFlags int8

// List flags.
const (
	OneOrMore ListFlags = 1 << iota
	RightRecursive
)

// ExprKind is an production rule
type ExprKind int8

// All expression kinds.
const (
	Empty        ExprKind = iota
	Optional              // of {Sub0}
	Choice                // of 2+ Sub
	Sequence              // of 2+ Sub
	Reference             // {Symbol}<{Args}>
	Assign                // {Name}={Sub0}
	Append                // {Name}+={Sub0}
	Arrow                 // {Sub0} -> {Name}/{ArrowFlags}
	Set                   // set({SetIndex = index in Model.Sets})
	StateMarker           // .{Name}
	Command               // stored in {Name}
	Lookahead             // (?= {Sub0} & {Sub1} ...)
	LookaheadNot          // !{Sub0}   inside (?= ...)
	List                  // of {Sub0}, separator={Sub1} (if present), also {ListFlags}

	// The following kinds can appear as children of a top-level Choice expression only (or be nested
	// in one another).
	Conditional // [{Predicate}] {Sub0}
	Prec        // {Sub0} %prec {Symbol}
)

var kindStr = map[ExprKind]string{
	Empty:        "Empty",
	Optional:     "Optional",
	Choice:       "Choice",
	Sequence:     "Sequence",
	Reference:    "Reference",
	Assign:       "Assign",
	Append:       "Append",
	Arrow:        "Arrow",
	Set:          "Set",
	StateMarker:  "StateMarker",
	Command:      "Command",
	Lookahead:    "Lookahead",
	LookaheadNot: "LookaheadNot",
	Conditional:  "Conditional",
	Prec:         "Prec",
	List:         "List",
}

func (k ExprKind) GoString() string {
	if val, ok := kindStr[k]; ok {
		return val
	}
	return fmt.Sprintf("unknown(%v)", k)
}

// CmdArgs defines which RHS symbols are available inside a semantic action.
type CmdArgs struct {
	Names  map[string]int
	MaxPos int // exclusive, 1-based
	Delta  int // Added to the final position to adjust for extracted middle rule actions.
}

// TokenSet is a grammar expression that resolves to a set of tokens.
type TokenSet struct {
	Kind   SetOp
	Symbol int
	Args   []Arg
	Sub    []*TokenSet
	Origin status.SourceNode
}

// ForEach visits all token set expressions in the tree.
func (ts *TokenSet) ForEach(consumer func(ts *TokenSet)) {
	consumer(ts)
	for _, sub := range ts.Sub {
		sub.ForEach(consumer)
	}
}

func (ts *TokenSet) String(m *Model) string {
	switch ts.Kind {
	case Any:
		return m.Ref(ts.Symbol, ts.Args)
	case First:
		return "first " + m.Ref(ts.Symbol, ts.Args)
	case Last:
		return "last " + m.Ref(ts.Symbol, ts.Args)
	case Follow:
		return "follow " + m.Ref(ts.Symbol, ts.Args)
	case Precede:
		return "precede " + m.Ref(ts.Symbol, ts.Args)
	case Complement:
		return fmt.Sprintf("~(%v)", ts.Sub[0].String(m))
	case Union, Intersection:
		var buf strings.Builder
		for i, sub := range ts.Sub {
			if i > 0 {
				if ts.Kind == Intersection {
					buf.WriteString(" & ")
				} else {
					buf.WriteString(" | ")
				}
			}

			text := sub.String(m)
			if sub.Kind == Union || sub.Kind == Intersection {
				text = fmt.Sprintf("(%v)", text)
			}
			buf.WriteString(text)
		}
		return buf.String()
	default:
		log.Fatalf("cannot stringify TokenSet Kind=%v", ts.Kind)
		return ""
	}
}

// SetOp is a set operator.
type SetOp int8

// Set operators.
const (
	Any SetOp = iota
	First
	Last
	Precede
	Follow
	Union
	Intersection
	Complement
)

// Arg provides a value for a template parameter.
type Arg struct {
	Param    int
	Value    string
	TakeFrom int // if Value == "", the value should be taken from this parameter
	Origin   status.SourceNode
}

func (a Arg) equal(oth Arg) bool {
	return a.Param == oth.Param && a.Value == oth.Value && a.TakeFrom == oth.TakeFrom
}

func (a Arg) String() string {
	if a.Value == "" {
		return fmt.Sprintf("%v=%v", a.Param, a.TakeFrom)
	}
	return fmt.Sprintf("%v=%q", a.Param, a.Value)
}

// Predicate is an expression which, given a template environment, evaluates to true or false.
type Predicate struct {
	Op     PredicateOp
	Sub    []*Predicate
	Param  int
	Value  string
	Origin status.SourceNode
}

// ForEach visits all predicate expressions in the tree.
func (p *Predicate) ForEach(consumer func(*Predicate)) {
	consumer(p)
	for _, sub := range p.Sub {
		sub.ForEach(consumer)
	}
}

func (p *Predicate) String(m *Model) string {
	switch p.Op {
	case Or, And:
		var buf strings.Builder
		for i, sub := range p.Sub {
			if i > 0 {
				if p.Op == And {
					buf.WriteString(" & ")
				} else {
					buf.WriteString(" | ")
				}
			}

			text := sub.String(m)
			if sub.Op == Or || sub.Op == And {
				text = fmt.Sprintf("(%v)", text)
			}
			buf.WriteString(text)
		}
		return buf.String()
	case Not:
		return fmt.Sprintf("!(%v)", p.Sub[0].String(m))
	case Equals:
		var param string
		if m != nil {
			param = m.Params[p.Param].Name
		} else {
			param = fmt.Sprintf("[%v]", p.Param)
		}
		return fmt.Sprintf("%v=%q", param, p.Value)
	default:
		log.Fatalf("cannot stringify Op=%v", p.Op)
		return ""
	}
}

func (p *Predicate) equal(oth *Predicate) bool {
	if p.Op != oth.Op || len(p.Sub) != len(oth.Sub) {
		return false
	}
	if p.Op == Equals {
		return p.Param == oth.Param && p.Value == oth.Value
	}
	for i, val := range p.Sub {
		if !val.equal(oth.Sub[i]) {
			return false
		}
	}
	return true
}

// PredicateOp is a predicate operator.
type PredicateOp int8

// Predicate operators.
const (
	Or PredicateOp = iota
	And
	Not
	Equals // true if {Param} == {Value}
)

// Simplify flattens lists and removes redundant nodes from a syntax expression (in-place).
func Simplify(e *Expr, deep bool) *Expr {
	if deep {
		for i, sub := range e.Sub {
			e.Sub[i] = Simplify(sub, deep)
		}
	}
	if e.Kind != Choice && e.Kind != Sequence {
		return e
	}
	var rewrite bool
	for _, sub := range e.Sub {
		rewrite = rewrite || e.Kind == sub.Kind || sub.Kind == Empty && e.Kind == Sequence
	}
	if rewrite {
		var out []*Expr
		for _, sub := range e.Sub {
			if sub.Kind == Empty && e.Kind == Sequence {
				continue
			}
			if e.Kind == sub.Kind {
				out = append(out, sub.Sub...)
			} else {
				out = append(out, sub)
			}
		}
		e.Sub = out
	}
	switch len(e.Sub) {
	case 0:
		return &Expr{Kind: Empty, Origin: e.Origin}
	case 1:
		return e.Sub[0]
	}
	return e
}

func checkOrDie(m *Model, stage string) {
	if err := Check(m); err != nil {
		log.Fatalf("%v, internal failure: %v", stage, err)
	}
}

// Check verifies the internal consistency of the model.
func Check(m *Model) error {
	for _, inp := range m.Inputs {
		if nt := m.Nonterms[inp.Nonterm]; len(nt.Params) > 0 {
			return status.Errorf(nt.Origin, "input nonterminals cannot be parametrized")
		}
	}
	for _, nt := range m.Nonterms {
		if err := checkExpr(m, nt.Value); err != nil {
			return err
		}
	}
	for _, set := range m.Sets {
		if err := checkSet(m, set); err != nil {
			return err
		}
	}
	return nil
}

func checkArgs(m *Model, sym int, args []Arg, origin status.SourceNode) error {
	if sym < len(m.Terminals) {
		if len(args) > 0 {
			return status.Errorf(origin, "terminals cannot have arguments")
		}
		return nil
	}
	nt := m.Nonterms[sym-len(m.Terminals)]
	var argIndex int
	for _, arg := range args {
		if m.Params[arg.Param].Lookahead {
			continue
		}
		if argIndex >= len(nt.Params) {
			return status.Errorf(origin, "too many arguments")
		}
		if p := nt.Params[argIndex]; p != arg.Param {
			return status.Errorf(origin, "invalid argument order, found %v instead of %v (%v vs %v)", m.Params[arg.Param].Name, m.Params[p].Name, args, nt.Params)
		}
		argIndex++
	}
	if argIndex < len(nt.Params) {
		return status.Errorf(origin, "too few arguments were provided")
	}
	return nil
}

func checkExpr(m *Model, expr *Expr) error {
	switch expr.Kind {
	case Reference:
		if err := checkArgs(m, expr.Symbol, expr.Args, expr.Origin); err != nil {
			return err
		}
	}
	for _, sub := range expr.Sub {
		if err := checkExpr(m, sub); err != nil {
			return err
		}
	}
	return nil
}

func checkSet(m *Model, set *TokenSet) error {
	switch set.Kind {
	case Any, First, Last, Precede, Follow:
		if err := checkArgs(m, set.Symbol, set.Args, set.Origin); err != nil {
			return err
		}
	}
	for _, sub := range set.Sub {
		if err := checkSet(m, sub); err != nil {
			return err
		}
	}
	return nil
}

func sliceEqual(a, b []string) bool {
	if len(a) != len(b) {
		return false
	}
	for i, ai := range a {
		if ai != b[i] {
			return false
		}
	}
	return true
}
