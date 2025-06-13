package syntax

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/ident"
)

// updateArgRefs updates the ArgRefs of all commands under `rule` to include newly extracted
// nonterminals in 'newNts'.
//
// newNts is a map from 1-based position within the rule to the new symbol behind that position.
func updateArgRefs(rule *Expr, newNts map[int]int) {
	rule.ForEach(Command, func(e *Expr) {
		if e.CmdArgs == nil {
			return
		}
		for pos, sym := range newNts {
			copied, exists := e.CmdArgs.ArgRefs[pos]
			if !exists {
				// The ArgRefs of mid rules do not reference symbols that come after them.
				continue
			}
			copied.Symbol = sym
			e.CmdArgs.ArgRefs[pos] = copied
		}
	})
}

// ExpandOptions contains the options for the Expand function.
type ExpandOptions struct {
	// OptionalType returns the type of an optional symbol s? or s_opt, where `t` is the type of the
	// symbol s.
	OptionalType func(t string) string

	// OptionalCmd returns the command to calculate the semantic value of an optional symbol s_opt,
	// where `t` is the type of the symbol s.
	OptionalCmd func(t string) string

	// ListType returns the type of the list symbol s* or s+, where `t` is the type of the element
	// symbol s.
	ListType func(t string) string

	// NewList returns the command to create a new list of the given type.
	NewList func(elemType string, elemPos int, listFlags ListFlags) string

	// Append returns the command to append an element to a list.
	Append func(elemPos, listPos int, listFlags ListFlags) string

	// DefaultValue returns the default value of the given type `t`.
	DefaultValue func(t string) string

	ExpansionLimit int
	ExpansionWarn  int
}

// DefaultExpandOptions returns the default ExpandOptions.
func DefaultExpandOptions() *ExpandOptions {
	return &ExpandOptions{
		ExpansionLimit: 65_536,
		ExpansionWarn:  256,
	}
}

// CcExpandOptions returns the ExpandOptions for generating C++ semantic actions.
func CcExpandOptions() *ExpandOptions {
	return &ExpandOptions{
		OptionalType: func(t string) string {
			if t == "" {
				return ""
			}
			return "std::optional<" + t + ">"
		},
		OptionalCmd: func(t string) string {
			// For cc the semantic action does not need the input type `t`.
			//
			// TODO: This involves copying the rhs value when constructing the std::optional. Example
			// generated code:
			//
			// ```cc
			// lhs.value = std::optional<std::string>(std::get<std::string>(rhs[0].value));
			// ```
			//
			// If this turns out to be a performance bottleneck, we should find a way to use move when
			// constructing the std::optional.
			return fmt.Sprintf(`{ $$ = $1; }`)
		},
		ListType: func(t string) string {
			if t == "" {
				return ""
			}
			return "std::vector<" + t + ">"
		},
		NewList: func(elemType string, elemPos int, listFlags ListFlags) string {
			if listFlags&OneOrMore != 0 {
				return fmt.Sprintf(`
				  auto& elem = $%v;
				  auto& mutable_elem = const_cast<std::remove_const<typename std::remove_reference<decltype(elem)>::type>::type&>(elem);
				  $$ = std::vector<%v>{std::move(mutable_elem)};
				`, elemPos, elemType)
			}
			return fmt.Sprintf(`$$ = std::vector<%v>{};`, elemType)
		},
		Append: func(elemPos, listPos int, listFlags ListFlags) string {
			return fmt.Sprintf(`{
				auto& list = $%v;
				auto& elem = $%v;
				auto& mutable_list = const_cast<std::remove_const<typename std::remove_reference<decltype(list)>::type>::type&>(list);
				auto& mutable_elem = const_cast<std::remove_const<typename std::remove_reference<decltype(elem)>::type>::type&>(elem);
				auto new_list = std::move(mutable_list);
				new_list.push_back(std::move(mutable_elem));
				$$ = std::move(new_list);
			}`, listPos, elemPos)
		},
		DefaultValue: func(t string) string {
			if t == "" {
				return ""
			}
			return t + "{}"
		},
	}
}

// Expand rewrites the grammar substituting extended notation clauses with equivalent
// context-free production forms. Every nonterminal becomes a choice of sequences (production
// rules), where each sequence can contain only StateMarker, Command, or Reference expressions.
// Production rules can be wrapped into Prec to communicate precedence. Empty sequences are
// replaced with an Empty expression.
//
// Specifically, this function:
// - instantiates nonterminals for lists, sets, and lookaheads
// - expands nested Choice expressions, replacing its rule with one rule per alternative
// - duplicates rules containing Optional, with and without the optional part
//
// Note: for now it leaves Assign, Append, and Arrow expressions untouched. The first two can
// contain references only. Arrow can contain a sub-sequence if it reports more than one
// symbol reference.
func Expand(m *Model, opts *ExpandOptions) error {
	var s status.Status
	e := &expander{
		Model: m,
		m:     make(map[string]int),
		perm:  make([]int, len(m.Nonterms)),
		reuse: make([]int, 0, 16),
		opts:  opts,
	}
	max := len(m.Nonterms)
	for i, nt := range m.Nonterms {
		e.curr = i
		e.perm[i] = i + e.extra
		switch nt.Value.Kind {
		case Choice:
			var out []*Expr
			for _, rule := range nt.Value.Sub {
				rules, err := e.expandRule(rule, opts)
				if err != nil {
					s.AddError(err)
				}
				out = append(out, rules...)
			}
			nt.Value.Sub = collapseEmpty(out)
		case Set, Lookahead:
			// Do not introduce new nonterminals for top-level sets and lookaheads.
		default:
			rules, err := e.expandRule(nt.Value, opts)
			if err != nil {
				s.AddError(err)
			}
			nt.Value = &Expr{
				Kind:   Choice,
				Sub:    collapseEmpty(rules),
				Origin: nt.Value.Origin,
			}
		}

		// Sort the inserted nonterminals inside the permutation.
		if nt.group > 0 && i+1 < max && nt.group == m.Nonterms[i+1].group {
			// Delay sorting until we are past all the instantiations of one template.
			continue
		}
		e.sortTail()
	}

	// Move the extracted nonterminals next to their first usage.
	m.Rearrange(e.perm)

	// Expand top expressions of all extracted nonterminals (except sets).
	for self, nt := range m.Nonterms {
		switch nt.Value.Kind {
		case Optional:
			// Note: this case facilitates 0..* lists extraction. All other optionals are handled by
			// expandRule.
			if nt.Value.Sub[0].Kind != Reference {
				return status.Errorf(nt.Value.Origin, "internal error: expecting an optional reference, but got %+v", nt.Value.Sub[0])
			}
			symbolType := getSymbolType(nt.Value.Sub[0], m)
			subs := []*Expr{nt.Value.Sub[0], {Kind: Empty, Origin: nt.Value.Origin}}
			// For the %empty rule, use an empty list as the semantic value.
			if e.opts.DefaultValue != nil && symbolType != "" {
				defaultVal := e.opts.DefaultValue(symbolType)
				subs[1] = &Expr{Kind: Command, Name: "$$ = " + defaultVal + ";", Origin: nt.Value.Origin, CmdArgs: &CmdArgs{MaxPos: 1}}
			}
			nt.Value = &Expr{
				Kind:   Choice,
				Sub:    subs,
				Origin: nt.Value.Origin,
			}
		case List:
			// Note: at this point all lists either have at least one element or have no separators.
			listFlags := nt.Value.ListFlags
			rr := listFlags&RightRecursive != 0
			nonEmpty := listFlags&OneOrMore != 0
			elem := nt.Value.Sub[0]
			origin := nt.Value.Origin
			rec := &Expr{Kind: Sequence, Origin: origin}
			listRef := &Expr{Kind: Reference, Symbol: len(m.Terminals) + self, Model: m, Origin: origin}
			rec.Sub = append(rec.Sub, listRef)
			if len(nt.Value.Sub) > 1 {
				if rr {
					rec = concat(origin, nt.Value.Sub[1], rec)
				} else {
					rec = concat(origin, rec, nt.Value.Sub[1])
				}
			}
			nt.Value = &Expr{
				Kind:   Choice,
				Origin: origin,
			}
			// Automatic value propagation works for lists of references only (with and without
			// separators). In every other sense this branch repeats the next one.
			if elem.Kind == Reference {
				// Add the recursion rule, e.g. `a_list: a_list a`.
				var recursion []*Expr
				if rr {
					recursion = append(recursion, elem, rec)
				} else {
					recursion = append(recursion, rec, elem)
				}
				elemType := getSymbolType(elem, m)
				if opts.Append != nil && elemType != "" {
					// Assign a new Pos for the list reference itself so that its semantic value can be
					// referenced.
					//
					// The position of the list reference only needs to be different from the element Pos,
					// instead of having to match the order between the listRef and the elem. For example,
					// consider the following rule:
					//
					//   start: a+ {...}
					//
					// `elem.Pos` is 1. Assuming we generate left-recursion rules for a_list:
					//
					//    a_list: a_list a
					//
					// listRef.Pos is 2 (elem.Pos + 1), even though the listRef "a_list" actually appears
					// before the "a". This is ok because Pos is only used to identify the symbols (and thus
					// only needs to be unique), and the only semantic action that uses `listRef.Pos` is
					// generated by `opts.Append`, which accepts both elemPos and listPos as arguments.
					listPos := elem.Pos + 1
					listRef.Pos = listPos
					argRefs := map[int]ArgRef{
						elem.Pos: ArgRef{Pos: elem.Pos, Symbol: elem.Symbol},
						listPos:  ArgRef{Pos: listPos, Symbol: listRef.Symbol},
					}
					code := opts.Append(elem.Pos, listPos, listFlags)
					cmdArgs := &CmdArgs{MaxPos: listPos + 1, ArgRefs: argRefs}
					recursion = append(recursion, &Expr{Kind: Command, Name: code, Origin: origin, CmdArgs: cmdArgs})
				}
				nt.Value.Sub = append(nt.Value.Sub, concat(origin, recursion...))

				// Add the base rule, e.g. `a_list: a`.
				var base []*Expr
				switch {
				case nonEmpty:
					base = append(base, elem)
					if opts.NewList != nil && elemType != "" {
						argRefs := map[int]ArgRef{
							elem.Pos: ArgRef{Pos: elem.Pos, Symbol: elem.Symbol},
						}
						base = append(base, &Expr{Kind: Command, Name: opts.NewList(elemType, elem.Pos, listFlags), Origin: origin, CmdArgs: &CmdArgs{MaxPos: elem.Pos + 1, ArgRefs: argRefs}})
					}
				case opts.NewList != nil && elemType != "":
					base = append(base, &Expr{Kind: Command, Name: opts.NewList(elemType, elem.Pos, listFlags), Origin: origin, CmdArgs: &CmdArgs{MaxPos: elem.Pos + 1}})
				default:
					base = append(base, &Expr{Kind: Empty, Origin: origin})
				}
				nt.Value.Sub = append(nt.Value.Sub, concat(origin, base...))
			} else if elem.Kind == Choice {
				if rr {
					nt.Value.Sub = append(nt.Value.Sub, multiConcat(origin, elem.Sub, []*Expr{rec})...)
				} else {
					nt.Value.Sub = append(nt.Value.Sub, multiConcat(origin, []*Expr{rec}, elem.Sub)...)
				}
				if nonEmpty {
					nt.Value.Sub = append(nt.Value.Sub, elem.Sub...)
				} else {
					nt.Value.Sub = append(nt.Value.Sub, &Expr{Kind: Empty, Origin: origin})
				}
			} else {
				if rr {
					nt.Value.Sub = append(nt.Value.Sub, concat(origin, elem, rec))
				} else {
					nt.Value.Sub = append(nt.Value.Sub, concat(origin, rec, elem))
				}
				if nonEmpty {
					nt.Value.Sub = append(nt.Value.Sub, elem)
				} else {
					nt.Value.Sub = append(nt.Value.Sub, &Expr{Kind: Empty, Origin: origin})
				}
			}
		}
	}
	checkOrDie(m, "after expanding syntax sugar")
	return s.Err()
}

type expander struct {
	*Model
	curr  int
	extra int
	perm  []int
	m     map[string]int // name -> index in Model.Nonterms

	start int // nonterminal, for sorting
	base  int
	reuse []int

	createdNts map[int]int // The non-terminals created the current rule. Position -> Symbol

	opts *ExpandOptions // Target-language-specific options during expansion.
}

func (e *expander) sortTail() {
	start := e.start
	base := e.base
	e.base = e.extra
	e.start = e.curr + 1

	size := e.extra - base
	if size == 0 {
		return
	}
	local := e.reuse[:0]
	for i := start; i <= e.curr; i++ {
		local = append(local, i)
	}
	for k := len(e.Nonterms) - size; k < len(e.Nonterms); k++ {
		local = append(local, k)
	}
	sort.Slice(local, func(i, j int) bool {
		return e.Nonterms[local[i]].Name < e.Nonterms[local[j]].Name
	})
	for k, nt := range local {
		e.perm[nt] = start + base + k
	}
	e.reuse = local // return for reuse
}

func (e *expander) extractNonterm(expr *Expr, nonTermType string) *Expr {
	name := ProvisionalName(expr, e.Model)
	if existing, ok := e.m[name]; ok && expr.Equal(e.Nonterms[existing].Value) {
		sym := len(e.Terminals) + existing
		return &Expr{Kind: Reference, Symbol: sym, Model: e.Model, Origin: expr.Origin}
	}

	if _, ok := e.m[name]; name == "" || ok {
		index := 1
		base := name
		if name == "" {
			base = e.Nonterms[e.curr].Name + "$"
		}
		for {
			name = fmt.Sprintf("%v%v", base, index)
			if _, ok := e.m[name]; !ok {
				break
			}
			index++
		}
	}

	sym := len(e.Terminals) + len(e.Nonterms)
	e.m[name] = len(e.Nonterms)
	nt := &Nonterm{
		Name:   name,
		Value:  expr,
		Origin: expr.Origin,
		Type:   nonTermType,
	}
	e.Nonterms = append(e.Nonterms, nt)
	e.extra++
	e.perm = append(e.perm, e.curr+e.extra)
	return &Expr{Kind: Reference, Symbol: sym, Model: e.Model, Origin: expr.Origin}
}

func (e *expander) expandRule(rule *Expr, opts *ExpandOptions) (expanded []*Expr, err status.Status) {
 	e.createdNts = make(map[int]int)
 	defer func() {
		if len(expanded) > opts.ExpansionLimit {
			if err == nil {
				err = status.Status{}
			}
			err.Errorf(rule.Origin, "expanding rule produced %v rules which exeeds the limit of %v. Refactor the rule to reduce expansion or increase the `expansionLimit` value", len(expanded), opts.ExpansionLimit)
		} else if len(expanded) > opts.ExpansionWarn {
			loc := rule.Origin.SourceRange()
			log.Printf("WARNING: Expanding rule produced %v rules which exeeds the warning threshold of %v. Refactor the rule to reduce expansion or increase the `expansionWarn` value. At %v.", len(expanded), opts.ExpansionWarn, loc.String())
		}{
		for _, rule := range expanded {
			updateArgRefs(rule, e.createdNts)
		}
	}()

	if rule.Kind == Prec {
		ret := e.expandExpr(rule.Sub[0])
		for i, val := range ret {
			ret[i] = &Expr{
				Kind:   Prec,
				Sub:    []*Expr{val},
				Symbol: rule.Symbol,
				Origin: rule.Origin,
				Model:  rule.Model,
			}
		}
		return ret, nil
	}

	return e.expandExpr(rule), nil
}

func (e *expander) expandExpr(expr *Expr) []*Expr {
	switch expr.Kind {
	case Empty:
		return []*Expr{expr}
	case Optional:
		return append(e.expandExpr(expr.Sub[0]), &Expr{Kind: Empty, Origin: expr.Origin})
	case Sequence:
		ret := []*Expr{{Kind: Empty, Origin: expr.Origin}}
		for _, sub := range expr.Sub {
			ret = multiConcat(expr.Origin, ret, e.expandExpr(sub))
		}
		return ret
	case Choice:
		var ret []*Expr
		for _, sub := range expr.Sub {
			ret = append(ret, e.expandExpr(sub)...)
		}
		return ret
	case Arrow, Assign, Append:
		ret := e.expandExpr(expr.Sub[0])
		for i, val := range ret {
			if val.Kind == Empty && expr.Kind != Arrow {
				continue
			}
			ret[i] = &Expr{
				Kind:       expr.Kind,
				Sub:        []*Expr{val},
				Name:       expr.Name,
				ArrowFlags: expr.ArrowFlags,
				Origin:     expr.Origin,
			}
		}
		return ret
	case Set, Lookahead:
		ret := e.extractNonterm(expr, "" /*nonTermType*/)
		ret.Pos = expr.Pos
		if expr.Kind == Set {
			e.createdNts[ret.Pos] = ret.Symbol
		}
		return []*Expr{ret}
	case List:
		out := &Expr{Kind: List, Origin: expr.Origin, ListFlags: expr.ListFlags}
		out.Sub = e.expandExpr(expr.Sub[0])
		if len(out.Sub) > 1 {
			// We support a choice of elements
			out.Sub = []*Expr{{Kind: Choice, Sub: out.Sub, Origin: expr.Origin}}
		}
		if len(expr.Sub) > 1 {
			sep := e.expandExpr(expr.Sub[1])
			if len(sep) > 1 {
				log.Fatal("inconsistent state, only simple separators are supported at this stage")
			}
			out.Sub = append(out.Sub, sep[0])
			out.ListFlags |= OneOrMore
		}
		var listType string
		// Calculate the list type for list of references. More complex structures, e.g.
		// (a b)*, (a? b)+, (a?)* do not propagate the type automatically.
		if expr.Sub[0].Kind == Reference {
			elemType := getSymbolType(expr.Sub[0], e.Model)
			if e.opts.ListType != nil {
				listType = e.opts.ListType(elemType)
			}
		}
		ret := e.extractNonterm(out, listType)
		if expr.ListFlags&OneOrMore == 0 && out.ListFlags&OneOrMore != 0 {
			// List structs like "(a separator ',')*"" generates the following two non-terminals:
			//
			// (1) a_separator_comma_listopt: a_separator_comma_list | %empty
			// (2) a_separator_comma_list: a_separator_comma_list ',' a | a
			//
			// We assign `listType` to "a_separator_comma_listopt" instead of using
			// `e.opts.OptionalType(listType)` so that empty lists share the same type, e.g.
			//
			// list_string {std::string} = (a separator ',')*[a_list] {
			//   // $a_list will be of type std::vector<std::string>. For the %empty case the list will
			//   // be empty instead of std::optional<std::vector<std::string>>.
			//   $$ = absl::StrJoin($a_list, ", ");
			// }
			ret = e.extractNonterm(&Expr{Kind: Optional, Sub: []*Expr{ret}, Origin: expr.Origin}, listType)
		}
		ret.Pos = expr.Pos
		e.createdNts[ret.Pos] = ret.Symbol
		return []*Expr{ret}
	}
	return []*Expr{expr}
}

func concat(origin status.SourceNode, list ...*Expr) *Expr {
	ret := &Expr{Kind: Sequence, Origin: origin}
	for _, el := range list {
		if el.Kind == Sequence {
			ret.Sub = append(ret.Sub, el.Sub...)
		} else if el.Kind != Empty {
			ret.Sub = append(ret.Sub, el)
		}
	}
	switch len(ret.Sub) {
	case 0:
		return &Expr{Kind: Empty, Origin: origin}
	case 1:
		return ret.Sub[0]
	}
	return ret
}

func multiConcat(origin status.SourceNode, a, b []*Expr) []*Expr {
	var ret []*Expr
	for _, a := range a {
		for _, b := range b {
			ret = append(ret, concat(origin, a, b))
		}
	}
	return ret
}

func collapseEmpty(list []*Expr) []*Expr {
	var empties int
	for _, r := range list {
		if r.Kind == Empty {
			empties++
		}
	}
	if empties <= 1 {
		return list
	}
	out := list[:0]
	var seen bool
	for _, r := range list {
		if r.Kind == Empty {
			if seen {
				continue
			}
			seen = true
		}
		out = append(out, r)
	}
	return out
}

// ProvisionalName produces a name for a grammar expression.
func ProvisionalName(expr *Expr, m *Model) string {
	switch expr.Kind {
	case Reference:
		if expr.Symbol < len(m.Terminals) {
			return ident.Produce(m.Terminals[expr.Symbol].Name, ident.CamelCase)
		}
		return m.Nonterms[expr.Symbol-len(m.Terminals)].Name
	case Optional:
		ret := ProvisionalName(expr.Sub[0], m)
		if ret != "" {
			ret += "opt"
		}
		return ret
	case Assign, Append, Arrow:
		return ProvisionalName(expr.Sub[0], m)
	case List:
		ret := ProvisionalName(expr.Sub[0], m)
		if ret == "" {
			return ""
		}
		if nonempty := expr.ListFlags&OneOrMore != 0; nonempty {
			ret += "_list"
		} else {
			ret += "_optlist"
		}
		if len(expr.Sub) > 1 {
			sep := ProvisionalName(expr.Sub[1], m)
			if sep != "" {
				ret = fmt.Sprintf("%v_%v_separated", ret, sep)
			} else {
				ret += "_withsep"
			}
		}
		return ret
	case Choice, Sequence:
		var cand *Expr
		for _, sub := range expr.Sub {
			switch sub.Kind {
			case Empty, StateMarker, Lookahead, Command:
				continue
			}
			if cand != nil {
				return ""
			}
			cand = sub
		}
		if cand != nil {
			return ProvisionalName(cand, m)
		}
	case Set:
		var sb strings.Builder
		sb.WriteString("setof_")
		appendSetName(m.Sets[expr.SetIndex], m, &sb)
		return sb.String()
	case Lookahead:
		var sb strings.Builder
		sb.WriteString("lookahead")
		for _, sub := range expr.Sub {
			sb.WriteByte('_')
			if sub.Kind == LookaheadNot {
				sb.WriteString("not")
				sub = sub.Sub[0]
			}
			sb.WriteString(m.Ref(sub.Symbol, nil /*args*/))
		}
		return sb.String()
	}
	return ""
}

func appendSetName(ts *TokenSet, m *Model, out *strings.Builder) {
	switch ts.Kind {
	case Any:
		out.WriteString(m.Ref(ts.Symbol, nil /*args*/))
	case First:
		out.WriteString("first_")
		out.WriteString(m.Ref(ts.Symbol, nil /*args*/))
	case Last:
		out.WriteString("last_")
		out.WriteString(m.Ref(ts.Symbol, nil /*args*/))
	case Follow:
		out.WriteString("follow_")
		out.WriteString(m.Ref(ts.Symbol, nil /*args*/))
	case Precede:
		out.WriteString("precede_")
		out.WriteString(m.Ref(ts.Symbol, nil /*args*/))
	case Complement:
		out.WriteString("not_")
		appendSetName(ts.Sub[0], m, out)
	case Union, Intersection:
		for i, sub := range ts.Sub {
			if i > 0 {
				if ts.Kind == Union {
					out.WriteString("_or_")
				} else {
					out.WriteString("_")
				}
			}
			appendSetName(sub, m, out)
		}
	default:
		log.Fatalf("cannot compute name for TokenSet Kind=%v", ts.Kind)
	}
}

func getSymbolType(expr *Expr, m *Model) string {
	if expr.Symbol < len(m.Terminals) {
		return m.Terminals[expr.Symbol].Type
	}
	return m.Nonterms[expr.Symbol-len(m.Terminals)].Type
}
