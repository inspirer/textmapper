package syntax_test

import (
	"fmt"
	"testing"

	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/token"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/dump"
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
		got := syntax.Simplify(tc.input, true /*deep*/)
		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("Simplify(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}

var equalTests = []struct {
	a, b *syntax.Expr
	want bool
}{
	{
		a:    &syntax.Expr{Kind: syntax.StateMarker, Name: "foo"},
		b:    &syntax.Expr{Kind: syntax.Command, Name: "foo"},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Command, Name: "foo"},
		b:    &syntax.Expr{Kind: syntax.Command, Name: "foo"},
		want: true,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Command, Name: "foo"},
		b:    &syntax.Expr{Kind: syntax.Command, Name: "foo2"},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Set, SetIndex: 1},
		b:    &syntax.Expr{Kind: syntax.Set, SetIndex: 2},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1},
		b:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1},
		want: true,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1},
		b:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1, Args: []syntax.Arg{{Param: 1}}},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1, Args: []syntax.Arg{{Param: 2}}},
		b:    &syntax.Expr{Kind: syntax.Reference, Symbol: 1, Args: []syntax.Arg{{Param: 1}}},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{{Kind: syntax.Empty}}},
		b:    &syntax.Expr{Kind: syntax.Sequence},
		want: false,
	},
	{
		a:    &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{{Kind: syntax.Empty}}},
		b:    &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{{Kind: syntax.Reference}}},
		want: false,
	},
}

func TestEqual(t *testing.T) {
	for _, tc := range equalTests {
		if got := tc.a.Equal(tc.b); got != tc.want {
			t.Errorf("Equal(%v,%v) = %v, want %v", tc.a, tc.b, got, tc.want)
		}
	}
	for _, tc := range parserTests {
		for _, nt := range tc.want.Nonterms {
			if got := nt.Value.Equal(nt.Value); !got {
				t.Errorf("selfEqual(%v) = %v, want true", nt.Value, got)
			}
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
	for p.curr != token.EOI {
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
	{`%flag T; %lookahead flag V = true; A {foo}: a B<T=V, V=true>; B<T,V>:[T!=123];`, &syntax.Model{
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
			{Name: "B", Params: []int{0, 1}, Value: &syntax.Expr{
				Kind: syntax.Conditional,
				Predicate: &syntax.Predicate{Op: syntax.Not, Sub: []*syntax.Predicate{
					{Op: syntax.Equals, Param: 0, Value: "123"},
				}},
				Sub: []*syntax.Expr{{Kind: syntax.Empty}},
			}},
		},
	}},
	{`%flag A; %flag B; input: [A==false && B || !A] a | b;`, &syntax.Model{
		Terminals: []string{"EOI", "a", "b"},
		Params: []syntax.Param{
			{Name: "A"},
			{Name: "B"},
		},
		Nonterms: []*syntax.Nonterm{
			{Name: "input", Value: &syntax.Expr{Kind: syntax.Choice, Sub: []*syntax.Expr{
				{Kind: syntax.Conditional, Sub: []*syntax.Expr{{Kind: syntax.Reference, Symbol: 1}},
					Predicate: &syntax.Predicate{
						Op: syntax.Or,
						Sub: []*syntax.Predicate{
							{Op: syntax.And, Sub: []*syntax.Predicate{
								{Op: syntax.Equals, Param: 0, Value: "false"},
								{Op: syntax.Equals, Param: 1, Value: "true"},
							}},
							{Op: syntax.Not, Sub: []*syntax.Predicate{
								{Op: syntax.Equals, Param: 0, Value: "true"},
							}},
						},
					},
				},
				{Kind: syntax.Reference, Symbol: 2},
			}}},
		},
	}},
	{`A: set(a & B | c | ~first B & precede B & last P & follow P & ~Q); B: z; P:; Q:;`, &syntax.Model{
		Terminals: []string{"EOI", "a", "c", "z"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Set, SetIndex: 0}},
			{Name: "B", Value: &syntax.Expr{Kind: syntax.Reference, Symbol: 3}},
			{Name: "P", Value: &syntax.Expr{Kind: syntax.Empty}},
			{Name: "Q", Value: &syntax.Expr{Kind: syntax.Empty}},
		},
		Sets: []*syntax.TokenSet{{
			Kind: syntax.Union,
			Sub: []*syntax.TokenSet{
				{Kind: syntax.Intersection, Sub: []*syntax.TokenSet{
					{Kind: syntax.Any, Symbol: 1},
					{Kind: syntax.Any, Symbol: 5},
				}},
				{Kind: syntax.Any, Symbol: 2},
				{Kind: syntax.Intersection, Sub: []*syntax.TokenSet{
					{Kind: syntax.Complement, Sub: []*syntax.TokenSet{
						{Kind: syntax.First, Symbol: 5},
					}},
					{Kind: syntax.Precede, Symbol: 5},
					{Kind: syntax.Last, Symbol: 6},
					{Kind: syntax.Follow, Symbol: 6},
					{Kind: syntax.Complement, Sub: []*syntax.TokenSet{
						{Kind: syntax.Any, Symbol: 7},
					}},
				}},
			},
		}},
	}},
	{`A: (?= A) a;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{
				{Kind: syntax.Lookahead, Sub: []*syntax.Expr{
					{Kind: syntax.Reference, Symbol: 2},
				}},
				{Kind: syntax.Reference, Symbol: 1},
			}}},
		},
	}},
	{`A: (?= P & !Q) a b; P: a; Q: b;`, &syntax.Model{
		Terminals: []string{"EOI", "a", "b"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Sequence, Sub: []*syntax.Expr{
				{Kind: syntax.Lookahead, Sub: []*syntax.Expr{
					{Kind: syntax.Reference, Symbol: 4},
					{Kind: syntax.LookaheadNot, Sub: []*syntax.Expr{
						{Kind: syntax.Reference, Symbol: 5},
					}},
				}},
				{Kind: syntax.Reference, Symbol: 1},
				{Kind: syntax.Reference, Symbol: 2},
			}}},
			{Name: "P", Value: &syntax.Expr{Kind: syntax.Reference, Symbol: 1}},
			{Name: "Q", Value: &syntax.Expr{Kind: syntax.Reference, Symbol: 2}},
		},
	}},
	{`%interface Q, P; A: a;`, &syntax.Model{
		Terminals: []string{"EOI", "a"},
		Nonterms: []*syntax.Nonterm{
			{Name: "A", Value: &syntax.Expr{Kind: syntax.Reference, Symbol: 1}},
		},
		Cats: []string{
			"Q",
			"P",
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
		stripSelfRef(got)
		stripOrigin(got)
		if diff := dump.Diff(tc.want, got); diff != "" {
			t.Errorf("parse(%v) produced diff (-want +got):\n%s", tc.input, diff)
		}
	}
}

func stripSelfRef(m *syntax.Model) {
	m.ForEach(-1, func(_ *syntax.Nonterm, expr *syntax.Expr) {
		expr.Model = nil
	})
}

func stripOrigin(m *syntax.Model) {
	m.ForEach(-1, func(_ *syntax.Nonterm, expr *syntax.Expr) {
		expr.Origin = nil
	})
	m.ForEach(syntax.Reference, func(_ *syntax.Nonterm, expr *syntax.Expr) {
		for i := range expr.Args {
			expr.Args[i].Origin = nil
		}
	})
	m.ForEach(syntax.Conditional, func(_ *syntax.Nonterm, expr *syntax.Expr) {
		expr.Predicate.ForEach(func(p *syntax.Predicate) {
			p.Origin = nil
		})
	})
	for _, set := range m.Sets {
		set.ForEach(func(ts *syntax.TokenSet) {
			for i := range ts.Args {
				ts.Args[i].Origin = nil
			}
			ts.Origin = nil
		})
	}
	for _, nt := range m.Nonterms {
		nt.Origin = nil
		nt.ClearGroup()
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
	var prev token.Type
	for tok := l.Next(); tok != token.EOI; tok = l.Next() {
		if tok == token.INVALID_TOKEN {
			return fmt.Errorf("%v: invalid token: %s", l.Line(), l.Text())
		}
		if tok != token.ID && !tm.IsSoftKeyword(tok) {
			prev = tok
			continue
		}
		copy := l
		if la := copy.Next(); la == token.ASSIGN || la == token.PLUSASSIGN {
			prev = tok
			continue
		}

		if isTerm(l.Text()) {
			if !seen[l.Text()] {
				out.Terminals = append(out.Terminals, l.Text())
			}
			seen[l.Text()] = true
		} else {
			if prev == token.EOI || prev == token.SEMICOLON {
				if seen[l.Text()] {
					return fmt.Errorf("redeclaration of " + l.Text())
				}
				seen[l.Text()] = true
				out.Nonterms = append(out.Nonterms, &syntax.Nonterm{Name: l.Text(), Origin: tokenOrigin(&l)})
			}
		}
		prev = tok
	}
	return nil
}

type node struct {
	offset, endoffset int
	line, col         int
}

// SourceRange implements status.SourceNode
func (n node) SourceRange() status.SourceRange {
	return status.SourceRange{
		Filename:  "input",
		Line:      n.line,
		Column:    n.col,
		Offset:    n.offset,
		EndOffset: n.endoffset,
	}
}

func tokenOrigin(l *tm.Lexer) node {
	start, end := l.Pos()
	return node{line: l.Line(), col: l.Column(), offset: start, endoffset: end}
}

type parser struct {
	lexer tm.Lexer
	curr  token.Type
	err   error
	out   *syntax.Model
}

func (p *parser) next() {
	p.curr = p.lexer.Next()
	if tm.IsSoftKeyword(p.curr) {
		p.curr = token.ID
	}
}

func (p *parser) lookahead() token.Type {
	l := p.lexer
	return l.Next()
}

func (p *parser) consumeIf(tok token.Type) bool {
	if p.curr == tok {
		p.next()
		return true
	}
	return false
}

func (p *parser) consume(tok token.Type) {
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
	case token.ID:
		p.parseNonterm()
		return
	case token.REM:
		p.next()
		switch p.lexer.Text() {
		case "flag", "lookahead":
			p.parseFlag()
			return
		case "input":
			p.next()
			if i, val := p.parseNontermRef(); val != nil {
				inp := syntax.Input{
					Nonterm: i - len(p.out.Terminals),
					NoEoi:   p.consumeIf(token.NOMINUSEOI),
				}
				p.out.Inputs = append(p.out.Inputs, inp)
			}
			p.consume(token.SEMICOLON)
			return
		case "interface":
			p.next()
			name := p.lexer.Text()
			p.consume(token.ID)
			p.out.Cats = append(p.out.Cats, name)
			for p.consumeIf(token.COMMA) {
				name := p.lexer.Text()
				p.consume(token.ID)
				p.out.Cats = append(p.out.Cats, name)
			}
			p.consume(token.SEMICOLON)
			return
		case "generate":
			p.errorf("TODO parse %v", p.lexer.Text())
		}
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
	p.consume(token.ID)
	var defaultVal string
	if p.consumeIf(token.ASSIGN) {
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
	p.consume(token.SEMICOLON)
}

func (p *parser) parseNonterm() {
	_, ret := p.parseNontermRef()
	if ret == nil {
		return
	}
	if p.consumeIf(token.LT) {
		ret.Params = append(ret.Params, p.parseParamRef())
		for p.consumeIf(token.COMMA) {
			ret.Params = append(ret.Params, p.parseParamRef())
		}
		p.consume(token.GT)
	}
	if p.curr == token.CODE {
		ret.Type = p.lexer.Text()
		p.next()
	}
	p.consume(token.COLON)
	ret.Value = &syntax.Expr{Kind: syntax.Choice, Origin: tokenOrigin(&p.lexer)}
	ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	for p.consumeIf(token.OR) {
		ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	}
	ret.Value = syntax.Simplify(ret.Value, true /*deep*/)
	p.consume(token.SEMICOLON)
}

func (p *parser) parseNontermRef() (int, *syntax.Nonterm) {
	name := p.lexer.Text()
	p.consume(token.ID)
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
	p.consume(token.ID)
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
	p.consume(token.ID)
	for i, val := range p.out.Params {
		if val.Name == name {
			return i
		}
	}
	p.errorf("param %q is not found", name)
	return 0
}

func (p *parser) parseRule() *syntax.Expr {
	var chain []*syntax.Expr
	if p.curr == token.LBRACK {
		chain = append(chain, &syntax.Expr{Kind: syntax.Conditional, Predicate: p.parsePredicate()})
	}

	ret := p.parseParts()
	if p.consumeIf(token.REM) {
		switch p.lexer.Text() {
		case "prec":
			p.next()
			chain = append(chain, &syntax.Expr{Kind: syntax.Prec, Symbol: p.parseTermRef(), Model: p.out})
		default:
			p.errorf("%%%s is not supported", p.lexer.Text())
		}
	}
	for p.consumeIf(token.MINUSGT) {
		name := p.lexer.Text()
		p.consume(token.ID)
		ret = &syntax.Expr{Kind: syntax.Arrow, Name: name, Sub: []*syntax.Expr{ret}}
	}

	for i := len(chain) - 1; i >= 0; i-- {
		chain[i].Sub = []*syntax.Expr{ret}
		ret = chain[i]
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
	return &syntax.Expr{Kind: syntax.Sequence, Sub: ret, Origin: tokenOrigin(&p.lexer)}
}

func (p *parser) parsePart() *syntax.Expr {
	switch p.curr {
	case token.ID:
		if la := p.lookahead(); la != token.PLUSASSIGN && la != token.ASSIGN {
			break
		}
		name := p.lexer.Text()
		p.next()
		var kind syntax.ExprKind
		switch p.curr {
		case token.PLUSASSIGN:
			kind = syntax.Append
		case token.ASSIGN:
			kind = syntax.Assign
		default:
			p.errorf("wrong assignment")
			return nil
		}
		origin := tokenOrigin(&p.lexer)
		p.next()
		inner := p.parseOpt()
		return &syntax.Expr{Kind: kind, Name: name, Sub: []*syntax.Expr{inner}, Origin: origin}
	case token.DOT:
		p.next()
		name := p.lexer.Text()
		p.consume(token.ID)
		return &syntax.Expr{Kind: syntax.StateMarker, Name: name}
	case token.CODE:
		code := p.lexer.Text()
		p.next()
		return &syntax.Expr{Kind: syntax.Command, Name: code}
	case token.LPARENQUESTASSIGN:
		p.next()
		ret := &syntax.Expr{Kind: syntax.Lookahead}
		for {
			not := p.consumeIf(token.EXCL)
			sym, _ := p.parseNontermRef()
			expr := &syntax.Expr{Kind: syntax.Reference, Symbol: sym}
			if not {
				expr = &syntax.Expr{Kind: syntax.LookaheadNot, Sub: []*syntax.Expr{expr}}
			}
			ret.Sub = append(ret.Sub, expr)
			if !p.consumeIf(token.AND) {
				break
			}
		}
		p.consume(token.RPAREN)
		return ret
	}
	return p.parseOpt()
}

func (p *parser) parseOpt() *syntax.Expr {
	inner := p.parsePrimary()
	if inner == nil {
		return nil
	}
	if p.curr == token.AS {
		p.errorf("'as' clauses are not supported")
	}
	if p.curr == token.QUEST {
		p.next()
		inner = &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{inner}}
	}
	return inner
}

func (p *parser) parseSymref() *syntax.Expr {
	origin := tokenOrigin(&p.lexer)
	if isTerm(p.lexer.Text()) {
		return &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef(), Origin: origin, Model: p.out}
	}
	sym, _ := p.parseNontermRef()
	var args []syntax.Arg
	if p.consumeIf(token.LT) {
		args = append(args, p.parseArg())
		for p.consumeIf(token.COMMA) {
			args = append(args, p.parseArg())
		}
		p.consume(token.GT)
	}
	return &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args, Origin: origin, Model: p.out}
}

func (p *parser) parseArg() syntax.Arg {
	origin := tokenOrigin(&p.lexer)
	param := p.parseParamRef()
	p.consume(token.ASSIGN)
	val := p.lexer.Text()
	if val == "true" || val == "false" {
		p.next()
		return syntax.Arg{Param: param, Value: val, Origin: origin}
	}
	return syntax.Arg{Param: param, TakeFrom: p.parseParamRef(), Origin: origin}
}

func (p *parser) parsePrimary() *syntax.Expr {
	var ret *syntax.Expr
	var sep *syntax.Expr
	switch p.curr {
	case token.ID:
		ret = p.parseSymref()
	case token.LPAREN:
		ret = &syntax.Expr{Kind: syntax.Choice, Origin: tokenOrigin(&p.lexer)}
		p.next()
		ret.Sub = append(ret.Sub, p.parseRule())
		if p.consumeIf(token.SEPARATOR) {
			sep = &syntax.Expr{Kind: syntax.Sequence, Origin: tokenOrigin(&p.lexer)}
			for {
				sym := &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef()}
				sep.Sub = append(sep.Sub, sym)
				if p.curr != token.ID {
					break
				}
			}
			p.consume(token.RPAREN)
			if p.curr != token.PLUS && p.curr != token.MULT {
				p.errorf("qualifier is expected")
			}
			break
		}
		for p.consumeIf(token.OR) {
			ret.Sub = append(ret.Sub, p.parseRule())
		}
		p.consume(token.RPAREN)
	case token.SET:
		p.next()
		pos := len(p.out.Sets)
		p.out.Sets = append(p.out.Sets, p.parseSet())
		ret = &syntax.Expr{Kind: syntax.Set, SetIndex: pos}
	default:
		return nil
	}
	for p.curr == token.PLUS || p.curr == token.MULT {
		var flags syntax.ListFlags
		if p.curr == token.PLUS {
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

func (p *parser) parsePredicate() *syntax.Predicate {
	p.consume(token.LBRACK)
	ret := p.parsePredicateAnd()
	for p.consumeIf(token.OROR) {
		if ret.Op != syntax.Or {
			ret = &syntax.Predicate{Op: syntax.Or, Sub: []*syntax.Predicate{ret}}
		}
		ret.Sub = append(ret.Sub, p.parsePredicateAnd())
	}
	p.consume(token.RBRACK)
	return ret
}

func (p *parser) parsePredicateAnd() *syntax.Predicate {
	ret := p.parsePredicatePrimary()
	for p.consumeIf(token.ANDAND) {
		if ret.Op != syntax.And {
			ret = &syntax.Predicate{Op: syntax.And, Sub: []*syntax.Predicate{ret}}
		}
		ret.Sub = append(ret.Sub, p.parsePredicatePrimary())
	}
	return ret
}

func (p *parser) parsePredicatePrimary() *syntax.Predicate {
	origin := tokenOrigin(&p.lexer)
	if p.consumeIf(token.EXCL) {
		return &syntax.Predicate{Op: syntax.Not, Sub: []*syntax.Predicate{
			{Op: syntax.Equals, Param: p.parseParamRef(), Value: "true", Origin: origin}}}
	}
	ret := &syntax.Predicate{Op: syntax.Equals, Param: p.parseParamRef(), Value: "true", Origin: origin}
	switch p.curr {
	case token.ASSIGNASSIGN:
		p.next()
		ret.Value = p.literal()
	case token.EXCLASSIGN:
		p.next()
		ret.Value = p.literal()
		ret = &syntax.Predicate{Op: syntax.Not, Sub: []*syntax.Predicate{ret}}
	}
	return ret
}

func (p *parser) literal() string {
	switch p.curr {
	case token.TRUE, token.FALSE, token.ICON:
		val := p.lexer.Text()
		p.next()
		return val
	}
	p.errorf("unexpected literal %q", p.lexer.Text())
	return ""
}

func (p *parser) parseSet() *syntax.TokenSet {
	p.consume(token.LPAREN)
	ret := p.parseSetAnd()
	for p.consumeIf(token.OR) {
		if ret.Kind != syntax.Union {
			ret = &syntax.TokenSet{Kind: syntax.Union, Sub: []*syntax.TokenSet{ret}}
		}
		ret.Sub = append(ret.Sub, p.parseSetAnd())
	}
	p.consume(token.RPAREN)
	return ret
}

func (p *parser) parseSetAnd() *syntax.TokenSet {
	ret := p.parseSetPrimary()
	for p.consumeIf(token.AND) {
		if ret.Kind != syntax.Intersection {
			ret = &syntax.TokenSet{Kind: syntax.Intersection, Sub: []*syntax.TokenSet{ret}}
		}
		ret.Sub = append(ret.Sub, p.parseSetPrimary())
	}
	return ret
}

func (p *parser) parseSetPrimary() *syntax.TokenSet {
	tilde := tokenOrigin(&p.lexer)
	compl := p.consumeIf(token.TILDE)
	if p.curr == token.LPAREN {
		ret := p.parseSet()
		if compl {
			ret = &syntax.TokenSet{Kind: syntax.Complement, Sub: []*syntax.TokenSet{ret}, Origin: tilde}
		}
		return ret
	}
	var op string
	if p.lookahead() == token.ID {
		op = p.lexer.Text()
		p.next()
	}
	ret := &syntax.TokenSet{}
	switch op {
	case "":
		ret.Kind = syntax.Any
	case "precede":
		ret.Kind = syntax.Precede
	case "follow":
		ret.Kind = syntax.Follow
	case "first":
		ret.Kind = syntax.First
	case "last":
		ret.Kind = syntax.Last
	default:
		p.errorf("unsupported operator %v", op)
	}
	expr := p.parseSymref()
	ret.Symbol, ret.Args = expr.Symbol, expr.Args
	if compl {
		ret = &syntax.TokenSet{Kind: syntax.Complement, Sub: []*syntax.TokenSet{ret}, Origin: tilde}
	}
	return ret
}
