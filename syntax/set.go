package syntax

import (
	"log"

	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/util/set"
)

// ResolveSets turns every top-level TokenSet in the grammar into a Union of terminal symbols.
//
// Note: the function is expected to work on an expanded grammar, without templates, lists etc.
func ResolveSets(m *Model) error {
	if len(m.Sets) == 0 {
		return nil
	}

	nullable := Nullable(m)
	rules := rules(m)

	// Create a reverse index of the rules' content.
	index := make(map[lalr.Sym][]usage)
	defs := make(map[lalr.Sym][]int) // sym -> rules
	for rule, r := range rules {
		defs[r.lhs] = append(defs[r.lhs], rule)
		for pos, sym := range r.rhs {
			index[sym] = append(index[sym], usage{rule, pos})
		}
	}

	type key struct {
		op SetOp
		lalr.Sym
	}
	terms := len(m.Terminals)
	cl := set.NewClosure(terms)
	sets := make(map[key]*set.FutureSet)
	var queue []key

	instantiate := func(op SetOp, sym lalr.Sym) *set.FutureSet {
		key := key{op, sym}
		if set, ok := sets[key]; ok {
			return set
		}

		switch op {
		case Any, First, Last:
			if int(sym) < terms {
				ret := cl.Add([]int{int(sym)})
				sets[key] = ret
				return ret
			}
		}
		ret := cl.Add(nil)
		sets[key] = ret
		queue = append(queue, key)
		return ret
	}

	var translate func(s *TokenSet) *set.FutureSet
	translate = func(s *TokenSet) *set.FutureSet {
		switch s.Kind {
		case Any, First, Last, Precede, Follow:
			return instantiate(s.Kind, lalr.Sym(s.Symbol))
		case Union:
			var sets []*set.FutureSet
			for _, sub := range s.Sub {
				sets = append(sets, translate(sub))
			}
			ret := cl.Add(nil)
			ret.Include(sets...)
			return ret
		case Intersection:
			var sets []*set.FutureSet
			for _, sub := range s.Sub {
				sets = append(sets, translate(sub))
			}
			return cl.Intersect(sets...)
		case Complement:
			return cl.Complement(translate(s.Sub[0]), s.Origin)
		default:
			log.Fatalf("invalid token set: %v", s.Kind)
		}
		return nil
	}

	var result []*set.FutureSet
	for _, set := range m.Sets {
		result = append(result, translate(set))
	}

	for len(queue) > 0 {
		key := queue[len(queue)-1]
		queue = queue[:len(queue)-1]

		switch key.op {
		case Any:
			for _, r := range defs[key.Sym] {
				for _, sym := range rules[r].rhs {
					sets[key].Include(instantiate(Any, sym))
				}
				if set := rules[r].set; set >= 0 {
					sets[key].Include(result[set])
				}
			}
		case First:
			for _, r := range defs[key.Sym] {
				for _, sym := range rules[r].rhs {
					sets[key].Include(instantiate(First, sym))
					if !nullable.Get(int(sym)) {
						break
					}
				}
				if set := rules[r].set; set >= 0 {
					sets[key].Include(result[set])
				}
			}
		case Last:
			for _, r := range defs[key.Sym] {
				rhs := rules[r].rhs
				for i := len(rhs) - 1; i >= 0; i-- {
					sym := rhs[i]
					sets[key].Include(instantiate(Last, sym))
					if !nullable.Get(int(sym)) {
						break
					}
				}
				if set := rules[r].set; set >= 0 {
					sets[key].Include(result[set])
				}
			}
		case Precede:
			for _, usage := range index[key.Sym] {
				rhs := rules[usage.rule].rhs
				var scoped bool
				for i := usage.pos - 1; i >= 0; i-- {
					sym := rhs[i]
					sets[key].Include(instantiate(Last, sym))
					if !nullable.Get(int(sym)) {
						scoped = true
						break
					}
				}
				if !scoped {
					sets[key].Include(instantiate(Precede, rules[usage.rule].lhs))
				}
			}
		case Follow:
			for _, usage := range index[key.Sym] {
				rhs := rules[usage.rule].rhs
				var scoped bool
				for i := usage.pos + 1; i < len(rhs); i++ {
					sym := rhs[i]
					sets[key].Include(instantiate(First, sym))
					if !nullable.Get(int(sym)) {
						scoped = true
						break
					}
				}
				if !scoped {
					sets[key].Include(instantiate(Follow, rules[usage.rule].lhs))
				}
			}
		}
	}

	if err := cl.Compute(); err != nil {
		var s status.Status
		for _, err := range err.(set.ClosureError) {
			s.Errorf(err.Origin, "set complement cannot transitively depend on itself")
		}
		return s.Err()
	}

	for i, set := range result {
		terminals := set.IntSet.Set
		if set.IntSet.Inverse {
			terminals = set.IntSet.BitSet(terms).Slice(nil)
		}

		out := &TokenSet{Kind: Union}
		for _, t := range terminals {
			out.Sub = append(out.Sub, &TokenSet{Kind: Any, Symbol: t})
		}
		m.Sets[i] = out
	}
	for _, nonterm := range m.Nonterms {
		if nonterm.Value.Kind == Set {
			terminals := m.Sets[nonterm.Value.SetIndex].Sub
			if len(terminals) == 0 {
				nonterm.Value = &Expr{Kind: Choice, Origin: nonterm.Value.Origin}
				nonterm.Value.Sub = append(nonterm.Value.Sub, &Expr{Kind: Empty, Origin: nonterm.Value.Origin})
				continue
			}
			nonterm.Value = &Expr{Kind: Choice, Origin: nonterm.Value.Origin}
			for _, t := range terminals {
				nonterm.Value.Sub = append(nonterm.Value.Sub, &Expr{Kind: Reference, Symbol: t.Symbol, Model: m, Origin: nonterm.Value.Origin})
			}
		}
	}
	return nil
}

type usage struct {
	rule int
	pos  int
}

type oneRule struct {
	lhs lalr.Sym
	rhs []lalr.Sym
	set int
}

func (r *oneRule) accept(impl *Expr) {
	switch impl.Kind {
	case Empty, StateMarker, Command:
		// OK
	case Sequence:
		for _, sub := range impl.Sub {
			r.accept(sub)
		}
	case Reference:
		r.rhs = append(r.rhs, lalr.Sym(impl.Symbol))
	case Arrow, Assign, Append, Prec:
		r.accept(impl.Sub[0])
	default:
		log.Fatalf("found %v inside a rule", impl.Kind)
	}
}

// rules extracts the subset of the grammar reachable via the first input as production rules.
func rules(m *Model) []oneRule {
	seen := make(map[int]bool)
	var queue []int

	enqueue := func(nonterm int) {
		if !seen[nonterm] {
			seen[nonterm] = true
			queue = append(queue, nonterm)
		}
	}
	for _, inp := range m.Inputs {
		if !inp.NoEoi {
			enqueue(inp.Nonterm)
			break
		}
	}
	var ret []oneRule
	for len(queue) > 0 {
		nt := queue[len(queue)-1]
		queue = queue[:len(queue)-1]
		val := m.Nonterms[nt].Value
		switch val.Kind {
		case Lookahead:
			ret = append(ret, oneRule{lhs: lalr.Sym(nt + len(m.Terminals)), set: -1})

			// Note: some nonterminals can be reachable via lookaheads. Pull in their rules.
			for _, sub := range val.Sub {
				if sub.Kind == LookaheadNot {
					sub = sub.Sub[0]
				}
				if sub.Symbol >= len(m.Terminals) {
					enqueue(sub.Symbol - len(m.Terminals))
				}
			}
		case Set:
			ret = append(ret, oneRule{lhs: lalr.Sym(nt + len(m.Terminals)), set: val.SetIndex})

			// Note: some nonterminals can be reachable via set expressions. Pull in their rules.
			m.Sets[val.SetIndex].ForEach(func(ts *TokenSet) {
				if ts.Symbol >= len(m.Terminals) {
					enqueue(ts.Symbol - len(m.Terminals))
				}
			})
		case Choice:
			for _, expr := range val.Sub {
				rule := oneRule{lhs: lalr.Sym(nt + len(m.Terminals)), set: -1}
				rule.accept(expr)
				for _, sym := range rule.rhs {
					if nt := int(sym) - len(m.Terminals); nt >= 0 {
						enqueue(nt)
					}
				}
				ret = append(ret, rule)
			}
		default:
			log.Fatalf("%v is not properly instantiated: %v", m.Nonterms[nt].Name, val.Kind)
		}
	}
	return ret
}
