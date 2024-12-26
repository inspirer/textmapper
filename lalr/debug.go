package lalr

import (
	"fmt"
	"strings"

	"github.com/inspirer/textmapper/util/container"
)

func (c *compiler) writeRule(r int, out *strings.Builder) {
	if r >= len(c.grammar.Rules) {
		// This is a runtime lookahead rule.
		nt := c.out.RuleSymbol[r]
		out.WriteString(c.grammar.Symbols[nt])
		out.WriteByte(':')
		return
	}

	rule := c.grammar.Rules[r]
	out.WriteString(c.grammar.Symbols[rule.LHS])
	out.WriteString(":")
	for _, sym := range rule.RHS {
		out.WriteString(" ")
		if sym.IsStateMarker() {
			out.WriteByte('.')
			out.WriteString(c.out.Markers[sym.AsMarker()].Name)
			continue
		}
		out.WriteString(c.grammar.Symbols[sym])
	}
}

func (c *compiler) writeItem(item int, out *strings.Builder) {
	i := item
	for c.right[i] >= 0 {
		i++
	}
	rule := c.grammar.Rules[-1-c.right[i]]
	pos := len(rule.RHS) - (i - item)
	for _, sym := range rule.RHS {
		if sym.IsStateMarker() {
			pos--
		}
	}

	out.WriteString(c.grammar.Symbols[rule.LHS])
	out.WriteString(":")
	var index int
	for _, sym := range rule.RHS {
		out.WriteString(" ")
		if sym.IsStateMarker() {
			out.WriteByte('.')
			out.WriteString(c.out.Markers[sym.AsMarker()].Name)
			continue
		}
		if index == pos {
			out.WriteString("_ ")
		}
		out.WriteString(c.grammar.Symbols[sym])
		index++
	}
	if pos == index {
		out.WriteString(" ")
		out.WriteString("_")
	}
}

func (c *compiler) exportDebugInfo() {
	final := make(map[int][]int)
	for i, state := range c.out.FinalStates {
		final[state] = append(final[state], i)
	}

	input := func(index int) string {
		inp := c.grammar.Inputs[index]
		ret := c.grammar.Symbols[inp.Nonterminal]
		if !inp.Eoi {
			ret += " no-eoi"
		}
		return ret
	}

	type conflict uint8
	conflicts := make([]conflict, c.grammar.Terminals)

	set := container.NewBitSet(len(c.right))
	reuse := make([]int, len(c.right))
	for _, s := range c.states {
		c.stateClosure(s, set)
		closure := set.Slice(reuse)

		var b strings.Builder
		fmt.Fprintf(&b, "state %v", s.index)

		var attrs []string
		if s.index < len(c.grammar.Inputs) {
			attrs = append(attrs, fmt.Sprintf("input %v", input(s.index)))
		}
		for _, inp := range final[s.index] {
			attrs = append(attrs, fmt.Sprintf("final %v", input(inp)))
		}
		if s.sourceState >= 0 {
			attrs = append(attrs, fmt.Sprintf("from %v on %v", s.sourceState, c.grammar.Symbols[s.symbol]))
		}
		if s.lr0 {
			if len(s.reduce) == 1 {
				attrs = append(attrs, "lr0 -> reduce")
			} else {
				attrs = append(attrs, "lr0 -> shift")
			}

		}
		if len(attrs) > 0 {
			fmt.Fprintf(&b, " (%v)", strings.Join(attrs, ", "))
		}
		b.WriteByte('\n')
		var reduce int
		for _, item := range closure {
			b.WriteString("\t")
			c.writeItem(item, &b)
			if r := c.right[item]; r < 0 {
				rule := c.grammar.Rules[-1-r]
				fmt.Fprintf(&b, " { reduce to %v", c.grammar.Symbols[rule.LHS])
				if !s.lr0 {
					b.WriteString(" lookahead [")
					for i, term := range s.la[reduce].Slice(nil) {
						if i > 0 {
							b.WriteByte(' ')
						}
						b.WriteString(c.grammar.Symbols[term])
					}
					b.WriteByte(']')
				}
				reduce++
				b.WriteString(" }")
			}
			b.WriteString("\n")
		}
		if len(closure) == 0 {
			b.WriteString("\t<empty>\n")
		}

		if len(s.dropped) > 0 {
			b.WriteString("\n\tDropped by .greedy:\n")

			for _, item := range s.dropped {
				b.WriteString("\t  ")
				c.writeItem(item, &b)
				b.WriteString("\n")
			}
		}

		if !s.lr0 || c.out.DefaultEnc.Action[s.index] == -1 /*shift*/ {
			for i := range conflicts {
				conflicts[i] = 0
			}
			for _, target := range s.shifts {
				sym := c.states[target].symbol
				if int(sym) < c.grammar.Terminals {
					conflicts[sym] = 1 // shift
				}
			}
			for i := range s.reduce {
				for _, term := range s.la[i].Slice(nil) {
					if conflicts[term] == 0 {
						conflicts[term] = 2 // reduce
					} else {
						conflicts[term] |= 4 // conflict
					}
				}
			}

			b.WriteString("\n\tAction: ")
			action := c.out.DefaultEnc.Action[s.index]
			switch {
			case action >= 0:
				b.WriteString("reduce `")
				c.writeRule(action, &b)
				b.WriteString("`\n")
			case action == -2:
				b.WriteString("error\n")
			case action == -1:
				b.WriteString("shift\n")
				for _, target := range s.shifts {
					t := c.states[target]
					fmt.Fprintf(&b, "\t\t%v => go to state %v\n", c.grammar.Symbols[t.symbol], target)
				}
			default:
				b.WriteString("lookahead\n")
				a := -action - 3
				for ; c.out.Lalr[a] >= 0; a += 2 {
					term := c.out.Lalr[a]
					fmt.Fprintf(&b, "\t\t%v => ", c.grammar.Symbols[term])
					switch action := c.out.Lalr[a+1]; {
					case action >= 0:
						b.WriteString("reduce `")
						c.writeRule(action, &b)
						b.WriteByte('`')
					case action == -1:
						fmt.Fprintf(&b, "shift, go to state %v", c.out.gotoState(s.index, term))
					case action == -2:
						b.WriteString("error")
					}
					switch conflicts[term] {
					case 5: // shift/reduce
						b.WriteString(" (resolved shift/reduce conflict)")
					case 6: // reduce/reduce
						b.WriteString(" (resolved reduce/reduce conflict)")
					}
					b.WriteByte('\n')
				}
			}
		}
		c.out.DebugInfo = append(c.out.DebugInfo, b.String())
	}
}
