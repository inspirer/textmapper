package syntax

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
		case Choice:
			// TODO expand every rule into one or more rules
		default:
			// TODO wrap a single rule into a choice
		}
	}
	return nil
}

// symbolBehind searches for a single reference behind expr and returns its symbol.
func symbolBehind(expr *Expr, m *Model) string {
	switch expr.Kind {
	case Reference:
		if expr.Symbol < len(m.Terminals) {
			return m.Terminals[expr.Symbol]
		}
		return m.Nonterms[expr.Symbol - len(m.Terminals)].Name
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
