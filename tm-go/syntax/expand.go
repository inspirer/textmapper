package syntax

import "log"

// Expand rewrites the grammar substituting extended notation clauses with equivalent
// context-free production forms. Every nonterminal becomes a choice of sequences (production
// rules), where each sequence can contain only StateMarker, Command, Reference, or Lookahead
// expressions. Production rules can be wrapped into Prec to communicate precedence. Empty
// sequences are replaced with an Empty expression.
//
// Specifically, this function:
// - instantiates nonterminals for lists and sets
// - expands nested Choice expressions, replacing its rule with one rule per alternative
// - duplicates rules containing Optional, with and without the optional part
//
// Note: for now it leaves Assign, Append, and Arrow expressions untouched. The first two can
// contain references only. Arrow can contain a sub-sequence if it reports more than one
// symbol reference.
func Expand(m *Model) error {
	for _, nt := range m.Nonterms {
		switch nt.Value.Kind {
		case List:
			// TODO expand lists
			log.Fatal("not implemented for lists")
		case Choice:
			var out []*Expr
			for _, rule := range nt.Value.Sub {
				out = append(out, expandRule(rule)...)
			}
			nt.Value.Sub = collapseEmpty(out)
		default:
			rules := expandRule(nt.Value)
			nt.Value = &Expr{
				Kind:   Choice,
				Sub:    collapseEmpty(rules),
				Origin: nt.Value.Origin,
			}
		}
	}
	return nil
}

func expandRule(rule *Expr) []*Expr {
	if rule.Kind == Prec {
		ret := expandExpr(rule.Sub[0])
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

	return expandExpr(rule)
}

func expandExpr(expr *Expr) []*Expr {
	switch expr.Kind {
	case Empty:
		return []*Expr{expr}
	case Optional:
		return append(expandExpr(expr.Sub[0]), &Expr{Kind: Empty})
	case Sequence:
		ret := []*Expr{{Kind: Empty}}
		for _, e := range expr.Sub {
			ret = multiConcat(ret, expandExpr(e))
		}
		return ret
	case Choice:
		var ret []*Expr
		for _, e := range expr.Sub {
			ret = append(ret, expandExpr(e)...)
		}
		return ret
	case Arrow, Assign, Append:
		ret := expandExpr(expr.Sub[0])
		for i, val := range ret {
			ret[i] = &Expr{
				Kind:   expr.Kind,
				Sub:    []*Expr{val},
				Name:   expr.Name,
				Origin: expr.Origin,
			}
		}
		return ret
	}
	return []*Expr{expr}
}

func concat(list ...*Expr) *Expr {
	ret := &Expr{Kind: Sequence}
	for _, el := range list {
		if el.Kind == Sequence {
			ret.Sub = append(ret.Sub, el.Sub...)
		} else if el.Kind != Empty {
			ret.Sub = append(ret.Sub, el)
		}
	}
	switch len(ret.Sub) {
	case 0:
		return &Expr{Kind: Empty}
	case 1:
		return ret.Sub[0]
	}
	return ret
}

func multiConcat(a, b []*Expr) []*Expr {
	var ret []*Expr
	for _, a := range a {
		for _, b := range b {
			ret = append(ret, concat(a, b))
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

// symbolBehind searches for a single reference behind expr and returns its symbol.
func symbolBehind(expr *Expr, m *Model) string {
	switch expr.Kind {
	case Reference:
		if expr.Symbol < len(m.Terminals) {
			return m.Terminals[expr.Symbol]
		}
		return m.Nonterms[expr.Symbol-len(m.Terminals)].Name
	case Optional, Assign, Append, Arrow:
		return symbolBehind(expr.Sub[0], m)
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
			return symbolBehind(cand, m)
		}
	}
	return ""
}

func listName(expr *Expr, m *Model) string {
	//nonempty := expr.ListFlags&OneOrMore != 0
	// TODO implement
	return ""
}
