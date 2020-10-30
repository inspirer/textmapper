package syntax_test

import (
	"fmt"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/tm"
	"github.com/inspirer/textmapper/tm-go/syntax"
	"github.com/inspirer/textmapper/tm-go/util/dump"
)

var simplifyTests = []struct {
	input *syntax.Expr
	want  *syntax.Expr
}{
	{
		input: &syntax.Expr{
			Kind: syntax.Sequence,
			Sub: []*syntax.Expr{
				{Kind: syntax.Sequence, Sub: []*syntax.Expr{{Kind: syntax.Empty}}},
				{Kind: syntax.Choice},
				{Kind: syntax.Empty},
			},
		},
		want: &syntax.Expr{Kind: syntax.Empty},
	},
}

func TestSimplify(t *testing.T) {
	for _, tc := range simplifyTests {
		got := syntax.Simplify(tc.input)
		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("Simplify(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}

// parse parses a simplified Textmapper grammar into syntax.Model ensuring structural
// but not semantic validity. Single-letter identifiers are reserved for terminals.
func parse(input string) (*syntax.Model, error) {
	// 1. Extract symbols.
	ret := new(syntax.Model)
	if err := initSymbols(input, ret); err != nil {
		return nil, err
	}

	// 2. Parse everything
	p := parser{out: ret}
	p.lexer.Init(input)
	p.next()
	for p.curr != tm.EOI {
		p.parseDecl()
		if p.err != nil {
			break
		}
	}
	return ret, p.err
}

var parserTests = []struct {
	input string
	want  *syntax.Model
}{
	{`A: a; B:;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Reference, Symbol: 1}},
			{Name: "B", Value: &syntax.Expr{Kind: syntax.Empty}},
		},
	}},
	{`A: b=a;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Assign, Name: "b", Sub: []*syntax.Expr{
				{Kind: syntax.Reference, Symbol: 1},
			}}},
		},
	}},
	{`A: a a -> foo;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Arrow, Name: "foo", Sub: []*syntax.Expr{
				{Kind: syntax.Sequence, Sub: []*syntax.Expr{
					{Kind: syntax.Reference, Symbol: 1},
					{Kind: syntax.Reference, Symbol: 1},
				}},
			}}},
		},
	}},
	{`A: b c+; B: (A separator b)*?;`, &syntax.Model{
		Terminals: []string{"EOI", "b", "c"},
		Nonterms: []*syntax.Nonterm{
			{
				Name: "A",
				Value: &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{
					{Kind: syntax.Reference, Symbol: 1},
					{Kind: syntax.List, ListFlags: syntax.OneOrMore, Sub: []*syntax.Expr{
						{Kind: syntax.Reference, Symbol: 2},
					}},
				}},
			},
			{
				Name: "B",
				Value: &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{
					{Kind: syntax.List, Sub: []*syntax.Expr{
						{Kind: syntax.Reference, Symbol: 3},
						{Kind: syntax.Reference, Symbol: 1}, // separator
					}},
				}},
			},
		},
	}},
	{`%flag T; %lookahead flag V = true; A {foo}: a B<T=V, V=true>; B<T,V>:;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Params: []syntax.Param{
			{Name: "T"},
			{Name: "V", DefaultValue: "true", Lookahead: true},
		},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Type: "{foo}", Value: &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{
				{Kind: syntax.Reference, Symbol: 1},
				{Kind: syntax.Reference, Symbol: 3, Args: []syntax.Arg{
					{Param: 0, TakeFrom: 1},
					{Param: 1, Value: "true"},
				}},
			}}},
			{Name: "B", Params: []int{0, 1}, Value: &syntax.Expr{Kind: syntax.Empty}},
		},
	}},
}

func TestParser(t *testing.T) {
	for _, tc := range parserTests {
		got, err := parse(tc.input)
		if err != nil {
			t.Errorf("parse(%v) failed with %v", tc.input, err)
			continue
		}
		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("parse(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}

func isTerm(s string) bool {
	return len(s) == 1 && s[0] >= 'a' && s[0] <= 'z'
}

func initSymbols(input string, out *syntax.Model) error {
	var l tm.Lexer
	l.Init(input)
	seen := make(map[string]bool)
	out.Terminals = []string{"EOI"}
	out.Nonterms = nil
	var prev tm.Token
	for tok := l.Next(); tok != tm.EOI; tok = l.Next() {
		if tok == tm.INVALID_TOKEN {
			return fmt.Errorf("%v: invalid token: %s", l.Line(), l.Text())
		}
		if tok != tm.ID {
			prev = tok
			continue
		}
		copy := l
		if la := copy.Next(); la == tm.ASSIGN || la == tm.PLUSASSIGN {
			prev = tok
			continue
		}

		if isTerm(l.Text()) {
			if !seen[l.Text()] {
				out.Terminals = append(out.Terminals, l.Text())
			}
			seen[l.Text()] = true
		} else {
			if prev == tm.EOI || prev == tm.SEMICOLON {
				if seen[l.Text()] {
					return fmt.Errorf("redeclaration of " + l.Text())
				}
				seen[l.Text()] = true
				out.Nonterms = append(out.Nonterms, &syntax.Nonterm{Name: l.Text()})
			}
		}
		prev = tok
	}
	return nil
}

type parser struct {
	lexer tm.Lexer
	curr  tm.Token
	err   error
	out   *syntax.Model
}

func (p *parser) next() {
	p.curr = p.lexer.Next()
	if tm.IsSoftKeyword(p.curr) {
		p.curr = tm.ID
	}
}

func (p *parser) lookahead() tm.Token {
	l := p.lexer
	return l.Next()
}

func (p *parser) consumeIf(tok tm.Token) bool {
	if p.curr == tok {
		p.next()
		return true
	}
	return false
}

func (p *parser) consume(tok tm.Token) {
	if p.curr != tok {
		p.errorf("found %v, while %v is expected", p.curr, tok)
	}
	p.next()
}

func (p *parser) errorf(format string, a ...interface{}) {
	if p.err != nil {
		return
	}
	l := p.lexer
	msg := fmt.Sprintf(format, a...)
	p.err = fmt.Errorf("line %v (at %v): %v", l.Line(), l.Text(), msg)
}

func (p *parser) parseDecl() {
	switch p.curr {
	case tm.ID:
		p.parseNonterm()
		return
	case tm.REM:
		p.next()
		switch p.lexer.Text() {
		case "flag", "lookahead":
			p.parseFlag()
			return
		}
		// TODO parse %input, %generate, %interface
	}
	p.errorf("syntax error")
}

func (p *parser) parseFlag() {
	var la bool
	if p.lexer.Text() == "lookahead" {
		la = true
		p.next()
	}
	if p.lexer.Text() != "flag" {
		p.errorf("'flag' is expected")
	}
	p.next()
	name := p.lexer.Text()
	p.consume(tm.ID)
	var defaultVal string
	if p.consumeIf(tm.ASSIGN) {
		defaultVal = p.lexer.Text()
		if defaultVal != "true" && defaultVal != "false" {
			p.errorf("true or false expected")
		}
		p.next()
	}
	p.out.Params = append(p.out.Params, syntax.Param{
		Name:         name,
		DefaultValue: defaultVal,
		Lookahead:    la,
	})
	p.consume(tm.SEMICOLON)
}

func (p *parser) parseNonterm() {
	_, ret := p.parseNontermRef()
	if ret == nil {
		return
	}
	if p.consumeIf(tm.LT) {
		ret.Params = append(ret.Params, p.parseParamRef())
		for p.consumeIf(tm.COMMA) {
			ret.Params = append(ret.Params, p.parseParamRef())
		}
		p.consume(tm.GT)
	}
	if p.curr == tm.CODE {
		ret.Type = p.lexer.Text()
		p.next()
	}
	p.consume(tm.COLON)
	ret.Value = &syntax.Expr{Kind: syntax.Choice}
	ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	for p.consumeIf(tm.OR) {
		ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	}
	ret.Value = syntax.Simplify(ret.Value)
	p.consume(tm.SEMICOLON)
}

func (p *parser) parseNontermRef() (int, *syntax.Nonterm) {
	name := p.lexer.Text()
	p.consume(tm.ID)
	for i, val := range p.out.Nonterms {
		if val.Name == name {
			return i + len(p.out.Terminals), val
		}
	}
	p.errorf("%q is not found", name)
	return 0, nil
}

func (p *parser) parseTermRef() int {
	name := p.lexer.Text()
	p.consume(tm.ID)
	if !isTerm(name) {
		p.errorf("terminal reference is expected (found %q)", name)
	}
	for i, val := range p.out.Terminals {
		if val == name {
			return i
		}
	}
	p.errorf("%q is not found", name)
	return 0
}

func (p *parser) parseParamRef() int {
	name := p.lexer.Text()
	p.consume(tm.ID)
	for i, val := range p.out.Params {
		if val.Name == name {
			return i
		}
	}
	p.errorf("param %q is not found", name)
	return 0
}

func (p *parser) parseRule() *syntax.Expr {
	// TODO parse predicate
	ret := p.parseParts()
	if p.consumeIf(tm.REM) {
		// TODO parse %prec
	}
	if p.consumeIf(tm.MINUSGT) {
		name := p.lexer.Text()
		p.consume(tm.ID)
		ret = &syntax.Expr{Kind: syntax.Arrow, Name: name, Sub: []*syntax.Expr{ret}}
	}
	return ret
}

func (p *parser) parseParts() *syntax.Expr {
	var ret []*syntax.Expr
	for {
		next := p.parsePart()
		if next == nil {
			break
		}
		ret = append(ret, next)
	}
	return &syntax.Expr{Kind: syntax.Sequence, Sub: ret}
}

func (p *parser) parsePart() *syntax.Expr {
	switch p.curr {
	case tm.ID:
		if la := p.lookahead(); la != tm.PLUSASSIGN && la != tm.ASSIGN {
			break
		}
		name := p.lexer.Text()
		p.next()
		var kind syntax.ExprKind
		switch p.curr {
		case tm.PLUSASSIGN:
			kind = syntax.Append
		case tm.ASSIGN:
			kind = syntax.Assign
		default:
			p.errorf("wrong assignment")
			return nil
		}
		p.next()
		inner := p.parseOpt()
		return &syntax.Expr{Kind: kind, Name: name, Sub: []*syntax.Expr{inner}}
	case tm.DOT:
		p.next()
		name := p.lexer.Text()
		p.consume(tm.ID)
		return &syntax.Expr{Kind: syntax.StateMarker, Name: name}
	case tm.CODE:
		code := p.lexer.Text()
		p.next()
		return &syntax.Expr{Kind: syntax.Command, Name: code}
	case tm.LOOKAHEAD:
		p.next()
		// TODO parse lookaheads
	}
	return p.parseOpt()
}

func (p *parser) parseOpt() *syntax.Expr {
	inner := p.parsePrimary()
	if inner == nil {
		return nil
	}
	if p.curr == tm.AS {
		p.next()
		// TODO parse as
	}
	if p.curr == tm.QUEST {
		p.next()
		inner = &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{inner}}
	}
	return inner
}

func (p *parser) parseSymref() *syntax.Expr {
	if isTerm(p.lexer.Text()) {
		return &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef()}
	}
	sym, _ := p.parseNontermRef()
	var args []syntax.Arg
	if p.consumeIf(tm.LT) {
		args = append(args, p.parseArg())
		for p.consumeIf(tm.COMMA) {
			args = append(args, p.parseArg())
		}
		p.consume(tm.GT)
	}
	return &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args}
}

func (p *parser) parseArg() syntax.Arg {
	param := p.parseParamRef()
	p.consume(tm.ASSIGN)
	val := p.lexer.Text()
	if val == "true" || val == "false" {
		p.next()
		return syntax.Arg{Param: param, Value: val}
	}
	return syntax.Arg{Param: param, TakeFrom: p.parseParamRef()}
}

func (p *parser) parsePrimary() *syntax.Expr {
	var ret *syntax.Expr
	var sep *syntax.Expr
	switch p.curr {
	case tm.ID:
		ret = p.parseSymref()
	case tm.LPAREN:
		p.next()
		ret = &syntax.Expr{Kind: syntax.Choice}
		ret.Sub = append(ret.Sub, p.parseRule())
		if p.consumeIf(tm.SEPARATOR) {
			sep = &syntax.Expr{Kind: syntax.Sequence}
			for {
				sym := &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef()}
				sep.Sub = append(sep.Sub, sym)
				if p.curr != tm.ID {
					break
				}
			}
			p.consume(tm.RPAREN)
			if p.curr != tm.PLUS && p.curr != tm.MULT {
				p.errorf("qualifier is expected")
			}
			break
		}
		for p.consumeIf(tm.OR) {
			ret.Sub = append(ret.Sub, p.parseRule())
		}
		p.consume(tm.RPAREN)
	case tm.SET:
		p.next()
		p.consume(tm.LPAREN)
		// TODO parse set
		p.consume(tm.RPAREN)
	default:
		return nil
	}
	for p.curr == tm.PLUS || p.curr == tm.MULT {
		var flags syntax.ListFlags
		if p.curr == tm.PLUS {
			flags = syntax.OneOrMore
		}
		p.next()
		sub := []*syntax.Expr{ret}
		if sep != nil {
			sub = append(sub, sep)
			sep = nil
		}
		ret = &syntax.Expr{Kind: syntax.List, ListFlags: flags, Sub: sub}
	}
	return ret
}
