package grammar

import (
	"fmt"
	"log"
	"sort"
	"strings"

	"github.com/inspirer/textmapper/lalr"
	"github.com/inspirer/textmapper/syntax"
)

// Note: all the methods in this file are convenience methods used in generation templates.

func (o *Options) IsEnabled(name string) bool {
	for _, v := range o.CustomImpl {
		if v == name {
			return false
		}
	}
	return true
}

// ValueString returns a comma-separated list of terminal IDs.
func (s *NamedSet) ValueString(g *Grammar) string {
	var ret []string
	for _, t := range s.Terminals {
		ret = append(ret, g.Syms[t].ID)
	}
	return strings.Join(ret, ", ")
}

// NontermRules is a pair of a nonterminal and its rules.
type NontermRules struct {
	Nonterm *syntax.Nonterm
	Rules   []*Rule
}

func (p *Parser) RulesByNonterm() []NontermRules {
	var ret []NontermRules
	m := make(map[lalr.Sym]int)
	for _, rule := range p.Rules {
		i, ok := m[rule.LHS]
		if !ok {
			i = len(ret)
			ret = append(ret, NontermRules{Nonterm: p.Nonterms[int(rule.LHS)-p.NumTerminals]})
			m[rule.LHS] = i
		}
		ret[i].Rules = append(ret[i].Rules, rule)
	}
	return ret
}

func (p *Parser) HasAssocValues() bool {
	for _, nt := range p.Nonterms {
		if nt.Type != "" {
			return true
		}
	}
	return false
}

func (p *Parser) UnionFields() []string {
	var ret []string
	seen := make(map[string]bool)
	for _, nt := range p.Nonterms {
		if nt.Type != "" && !seen[nt.Type] {
			ret = append(ret, nt.Type)
			seen[nt.Type] = true
		}
	}
	return ret
}

func (p *Parser) HasInputAssocValues() bool {
	for _, inp := range p.Inputs {
		if p.Nonterms[inp.Nonterm].Type != "" {
			return true
		}
	}
	return false
}

func (p *Parser) HasMultipleUserInputs() bool {
	var count int
	for _, inp := range p.Inputs {
		if inp.Synthetic {
			continue
		}
		count++
	}
	return count > 1
}

func (p *Parser) HasActions() bool {
	for _, r := range p.Rules {
		if r.Action > 0 {
			act := p.Actions[r.Action]
			if len(act.Report) > 0 || act.Code != "" {
				return true
			}
		}
	}
	return len(p.Tables.Lookaheads) > 0
}

func (p *Parser) HasActionsWithReport() bool {
	for _, r := range p.Rules {
		if r.Action > 0 {
			act := p.Actions[r.Action]
			if len(act.Report) > 0 {
				return true
			}
		}
	}
	return false
}

// Tokens returns all lexical tokens defined in the grammar.
func (g *Grammar) Tokens() []Symbol {
	return g.Syms[:g.NumTokens]
}

// TokensWithoutPrec returns all lexical tokens defined in the grammar that don't participate in
// precedence resolution. This method facilitates grammar conversion into Bison-like syntax.
func (g *Grammar) TokensWithoutPrec() []Symbol {
	var ret []Symbol
	seen := make(map[int]bool)
	for _, prec := range g.Parser.Prec {
		for _, term := range prec.Terminals {
			seen[int(term)] = true
		}
	}
	for _, sym := range g.Syms[:g.NumTokens] {
		if !seen[sym.Index] {
			ret = append(ret, sym)
		}
	}
	return ret
}

// ReportTokens returns a list of tokens that need to be injected into the AST.
func (g *Grammar) ReportTokens(space bool) []Symbol {
	var ret []Symbol
	for _, t := range g.Parser.MappedTokens {
		isSpace := g.Syms[t.Token].Space || g.Syms[t.Token].Name == "invalid_token"
		if isSpace == space {
			ret = append(ret, g.Syms[t.Token])
		}
	}
	return ret
}

func (g *Grammar) ReportsInvalidToken() bool {
	for _, t := range g.Parser.MappedTokens {
		if g.Syms[t.Token].Name == "invalid_token" {
			return true
		}
	}
	return false
}

// SpaceActions returns a sorted list of space-only actions.
func (g *Grammar) SpaceActions() []int {
	var ret []int
	for _, a := range g.Lexer.Actions {
		if a.Space {
			ret = append(ret, a.Action)
		}
	}
	sort.Ints(ret)
	return ret
}

// ExprString returns a user-friendly rendering of a given rule.
func (g *Grammar) ExprString(e *syntax.Expr) string {
	switch e.Kind {
	case syntax.Empty:
		return "%empty"
	case syntax.Prec:
		return g.ExprString(e.Sub[0]) + " %prec " + g.Syms[e.Symbol].ID
	case syntax.Assign, syntax.Append, syntax.Arrow:
		return g.ExprString(e.Sub[0])
	case syntax.Sequence:
		var buf strings.Builder
		for _, sub := range e.Sub {
			inner := g.ExprString(sub)
			if inner == "" || inner == "%empty" {
				continue
			}
			if buf.Len() > 0 {
				buf.WriteByte(' ')
			}
			buf.WriteString(inner)
		}
		if buf.Len() == 0 {
			return "%empty"
		}
		return buf.String()
	case syntax.Reference:
		return e.String()
	case syntax.StateMarker:
		return "/*." + e.Name + "*/"
	case syntax.Command:
		return ""
	default:
		log.Fatalf("cannot stringify kind=%v", e.Kind)
		return ""
	}
}

// RuleString returns a user-friendly rendering of a given rule.
func (g *Grammar) RuleString(r Rule) string {
	var sb strings.Builder
	fmt.Fprintf(&sb, "%v :", g.Syms[r.LHS].Name)
	for _, sym := range r.RHS {
		sb.WriteByte(' ')
		if sym.IsStateMarker() {
			sb.WriteByte('.')
			sb.WriteString(g.Parser.Tables.Markers[sym.AsMarker()].Name)
			continue
		}
		sb.WriteString(g.Syms[sym].Name)
	}
	if r.Precedence > 0 {
		sb.WriteString(" %prec ")
		sb.WriteString(g.Syms[r.Precedence].Name)
	}
	return sb.String()
}

func (g *Grammar) NontermID(nonterm int) string {
	return g.Syms[g.NumTokens+nonterm].ID
}

func (g *Grammar) NeedsSession() bool {
	return len(g.Parser.Tables.Lookaheads) > 0 && (g.Options.RecursiveLookaheads || g.Options.Cancellable)
}

func (g *Grammar) HasTrailingNulls(r Rule) bool {
	for i := len(r.RHS) - 1; i >= 0; i-- {
		sym := r.RHS[i]
		if sym.IsStateMarker() {
			continue
		}
		return g.Syms[sym].CanBeNull
	}
	return false
}

func (g *Grammar) AllFlags() []string {
	var ret []string
	ret = append(ret, g.Lexer.UsedFlags...)
	ret = append(ret, g.Parser.UsedFlags...)
	sort.Strings(ret)

	// Remove duplicates.
	var prev string
	in := ret
	ret = ret[:0]
	for _, str := range in {
		if str == prev {
			continue
		}
		prev = str
		ret = append(ret, str)
	}
	return ret
}

// FlexTranslate returns a translation table from token IDs returned by Flex to
// Textmapper token indices.
func (g *Grammar) FlexTranslate() []int {
	var maxFlexID int
	for _, sym := range g.Syms[:g.NumTokens] {
		maxFlexID = max(maxFlexID, sym.FlexID)
	}

	const invalidToken = 2
	ret := make([]int, maxFlexID+1)
	for i := range ret {
		ret[i] = invalidToken
	}
	ret[0] = 0 // EOI
	for i, sym := range g.Syms[:g.NumTokens] {
		ret[sym.FlexID] = i
	}
	return ret
}
