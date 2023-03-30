package syntax

import (
	"log"

	"github.com/inspirer/textmapper/util/container"
)

// Nullable computes and returns the set of nullable symbols in a given model.
//
// Note: this function does not instantiate templates but does some approximation if they are
// present by treating all conditional productions non-nullable.
func Nullable(m *Model) container.BitSet {
	ret := container.NewBitSet(len(m.Terminals) + len(m.Nonterms))

	terms := len(m.Terminals)
	for {
		var keepGoing bool
		for i, nt := range m.Nonterms {
			if ret.Get(terms + i) {
				continue
			}
			if isNullable(nt.Value, ret) {
				ret.Set(terms + i)
				keepGoing = true
			}
		}
		if !keepGoing {
			break
		}
	}
	return ret
}

func isNullable(expr *Expr, nullable container.BitSet) bool {
	switch expr.Kind {
	case Empty, Optional, StateMarker, Command, Lookahead:
		return true
	case Set:
		return false
	case List:
		if expr.ListFlags&OneOrMore == 0 {
			return true
		}
		fallthrough
	case Assign, Append, Arrow, Prec:
		return isNullable(expr.Sub[0], nullable)
	case Choice:
		for _, c := range expr.Sub {
			if isNullable(c, nullable) {
				return true
			}
		}
		return len(expr.Sub) == 0
	case Sequence:
		for _, c := range expr.Sub {
			if !isNullable(c, nullable) {
				return false
			}
		}
		return true
	case Reference:
		return nullable.Get(expr.Symbol)
	case Conditional, LookaheadNot:
		// Note: these are unexpected
		return false
	default:
		log.Fatal("invariant failure")
		return false
	}
}
