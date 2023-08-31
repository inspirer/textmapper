package syntax

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/ident"
)

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
func Expand(m *Model) error {
	e := &expander{
		Model: m,
		m:     make(map[string]int),
		perm:  make([]int, len(m.Nonterms)),
		reuse: make([]int, 0, 16),
	}
	max := len(m.Nonterms)
	for i, nt := range m.Nonterms {
		e.curr = i
		e.perm[i] = i + e.extra
		switch nt.Value.Kind {
		case Choice:
			var out []*Expr
			for _, rule := range nt.Value.Sub {
				out = append(out, e.expandRule(rule)...)
			}
			nt.Value.Sub = collapseEmpty(out)
		case Set, Lookahead:
			// Do not introduce new nonterminals for top-level sets and lookaheads.
		default:
			rules := e.expandRule(nt.Value)
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
			// Note: this case facilitates 0..* lists extraction.
			nt.Value = &Expr{
				Kind: Choice,
				Sub: []*Expr{
					nt.Value.Sub[0],
					{Kind: Empty, Origin: nt.Value.Origin},
				},
				Origin: nt.Value.Origin,
			}
		case List:
			// Note: at this point all lists either have at least one element or have no separators.
			rr := nt.Value.ListFlags&RightRecursive != 0
			nonEmpty := nt.Value.ListFlags&OneOrMore != 0
			elem := nt.Value.Sub[0]
			origin := nt.Value.Origin
			rec := &Expr{Kind: Sequence, Origin: origin}
			rec.Sub = append(rec.Sub, &Expr{Kind: Reference, Symbol: len(m.Terminals) + self, Model: m, Origin: origin})
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
			if elem.Kind == Choice {
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
	return nil
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

func (e *expander) extractNonterm(expr *Expr) *Expr {
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
	}
	e.Nonterms = append(e.Nonterms, nt)
	e.extra++
	e.perm = append(e.perm, e.curr+e.extra)
	return &Expr{Kind: Reference, Symbol: sym, Model: e.Model, Origin: expr.Origin}
}

func (e *expander) expandRule(rule *Expr) []*Expr {
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
		return ret
	}

	return e.expandExpr(rule)
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
		ret := e.extractNonterm(expr)
		ret.Pos = expr.Pos
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
		ret := e.extractNonterm(out)
		if expr.ListFlags&OneOrMore == 0 && out.ListFlags&OneOrMore != 0 {
			ret = e.extractNonterm(&Expr{Kind: Optional, Sub: []*Expr{ret}, Origin: expr.Origin})
		}
		ret.Pos = expr.Pos
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
			return ident.Produce(m.Terminals[expr.Symbol], ident.CamelCase)
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
