package syntax_test

import (
	"fmt"
	"strings"
	"testing"

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
	p := parser{lexer: lexer{input: input}, out: ret}
	p.next()
	for p.curr != EOI {
		p.parseDecl()
		if p.lexer.err != nil {
			break
		}
	}
	return ret, p.lexer.err
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

func TestLexer(t *testing.T) {
	input := `A: a B c(b .foo|C)* {as}; B: Q<T="true"> set(B) %prec z; C: set(B); %input C;`
	l := lexer{input: input}
	var got []string
	for l.next() != EOI {
		got = append(got, l.text())
	}
	if l.err != nil {
		t.Fatalf("lexer(%v) failed with %v", input, l.err)
	}

	want := []string{"A", ":", "a", "B", "c", "(", "b", ".", "foo", "|", "C", ")", "*", "{as}", ";",
		"B", ":", "Q", "<", "T", "=", `"true"`, ">", "set", "(", "B", ")",
		"%", "prec", "z", ";",
		"C", ":", "set", "(", "B", ")", ";",
		"%", "input", "C", ";"}
	if diff := dump.Diff(want, got); diff != "" {
		t.Errorf("next(%v) produced diff (-want +got):\n%s", input, diff)
	}

	// Test failures.
	l = lexer{input: `A - > g;`}
	for l.next() != EOI {
	}
	wantErr := fmt.Errorf("unexpected input: %s", "A ▶- > g;")
	if diff := dump.Diff(wantErr, l.err); diff != "" {
		t.Errorf("lexer.err(%v) produced diff (-want +got):\n%s", l.input, diff)
	}
}

type token uint16

const (
	EOI token = 0

	ID token = iota + 256
	TERM
	NAME
	CODE
	LITERAL
	PLUSEQ
	LOOKAHEAD
	ARROW
	SEPARATOR
	SET
	AS
)

var tokenStr = map[token]string{
	EOI:       "EOI",
	ID:        "ID",
	TERM:      "TERM",
	NAME:      "NAME",
	CODE:      "CODE",
	LITERAL:   "LITERAL",
	PLUSEQ:    "'+='",
	LOOKAHEAD: "'(?='",
	ARROW:     "'->'",
	SEPARATOR: "separator",
	SET:       "set",
	AS:        "as",
}

func (t token) String() string {
	if val, ok := tokenStr[t]; ok {
		return val
	}
	if t < 256 {
		return fmt.Sprintf("'%c'", t)
	}
	return "UNKNOWN"
}

// lexer is a simplified lexer for the textual representation of syntax.Model, supporting
// a subset of the Textmapper language tokens.
type lexer struct {
	input      string
	tokenStart int
	offset     int
	err        error
}

func (l *lexer) text() string {
	return l.input[l.tokenStart:l.offset]
}

func (l *lexer) next() token {
restart:
	l.tokenStart = l.offset
	if l.offset == len(l.input) {
		return EOI
	}
	start := l.offset
	s := l.input[start:]
	ch := s[0]
	if ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' {
		for ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' {
			l.offset++
			if l.offset >= len(l.input) {
				break
			}
			ch = l.input[l.offset]
		}
		if strings.HasPrefix(l.input[l.offset:], "=") || strings.HasPrefix(l.input[l.offset:], "+=") {
			return NAME
		}
		if l.offset-l.tokenStart == 1 && s[0] >= 'a' && s[0] <= 'z' {
			return TERM
		}
		switch l.text() {
		case "separator":
			return SEPARATOR
		case "set":
			return SET
		case "as":
			return AS
		}
		return ID
	}
	switch ch {
	case ' ', '\t', '\n', '\r':
		l.offset++
		goto restart
	case '-':
		if strings.HasPrefix(s, "->") {
			l.offset += 2
			return ARROW
		}
	case ';', ':', '(', ')', '|', '&', '?', '*', '+', '<', '>', ',', '=', '$', '%', '.':
		if strings.HasPrefix(s, "+=") {
			l.offset += 2
			return PLUSEQ
		}
		if strings.HasPrefix(s, "(?=") {
			l.offset += 3
			return LOOKAHEAD
		}
		l.offset++
		return token(ch)
	case '{':
		if i := strings.IndexByte(s, '}'); i >= 0 {
			l.offset += i + 1
			return CODE
		}
	case '"':
		if i := strings.IndexByte(s[1:], '"'); i >= 0 {
			l.offset += i + 2
			return LITERAL
		}
	}

	// Unexpected input.
	l.err = fmt.Errorf("unexpected input: %v", l.input[:l.tokenStart]+"▶"+l.input[l.tokenStart:])
	l.offset = len(l.input)
	return EOI
}

func initSymbols(input string, out *syntax.Model) error {
	l := lexer{input: input}
	seen := make(map[string]bool)
	out.Terminals = []string{"EOI"}
	out.Nonterms = nil
	var prev token
	for tok := l.next(); tok != EOI; tok = l.next() {
		switch tok {
		case TERM:
			if !seen[l.text()] {
				out.Terminals = append(out.Terminals, l.text())
			}
			seen[l.text()] = true
		case ID:
			if prev == EOI || prev == ';' {
				if seen[l.text()] {
					if l.err != nil {
						return l.err
					}
					return fmt.Errorf("redeclaration of " + l.text())
				}
				seen[l.text()] = true
				out.Nonterms = append(out.Nonterms, &syntax.Nonterm{Name: l.text()})
			}
		}
		prev = tok
	}
	return l.err
}

type parser struct {
	lexer lexer
	curr  token
	out   *syntax.Model
}

func (p *parser) next() {
	p.curr = p.lexer.next()
}

func (p *parser) consumeIf(tok token) bool {
	if p.curr == tok {
		p.next()
		return true
	}
	return false
}

func (p *parser) consume(tok token) {
	if p.curr != tok {
		p.errorf("found %v, while %v is expected", p.curr, tok)
	}
	p.next()
}

func (p *parser) errorf(format string, a ...interface{}) {
	if p.lexer.err != nil {
		return
	}
	l := p.lexer
	msg := fmt.Sprintf(format, a...)
	p.lexer.err = fmt.Errorf("%v: %v", msg, l.input[:l.tokenStart]+"▶"+l.input[l.tokenStart:])
}

func (p *parser) parseDecl() {
	switch p.curr {
	case ID:
		p.parseNonterm()
		return
	case '%':
		p.next()
		switch p.lexer.text() {
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
	if p.lexer.text() == "lookahead" {
		la = true
		p.next()
	}
	if p.lexer.text() != "flag" {
		p.errorf("'flag' is expected")
	}
	p.next()
	name := p.lexer.text()
	if !p.consumeIf(NAME) {
		p.consume(ID)
	}
	var defaultVal string
	if p.consumeIf('=') {
		defaultVal = p.lexer.text()
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
	p.consume(';')
}

func (p *parser) parseNonterm() {
	_, ret := p.parseNontermRef()
	if p.consumeIf('<') {
		ret.Params = append(ret.Params, p.parseParamRef())
		for p.consumeIf(',') {
			ret.Params = append(ret.Params, p.parseParamRef())
		}
		p.consume('>')
	}
	if p.curr == CODE {
		ret.Type = p.lexer.text()
		p.next()
	}
	p.consume(':')
	ret.Value = &syntax.Expr{Kind: syntax.Choice}
	ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	for p.consumeIf('|') {
		ret.Value.Sub = append(ret.Value.Sub, p.parseRule())
	}
	ret.Value = syntax.Simplify(ret.Value)
	p.consume(';')
}

func (p *parser) parseNontermRef() (int, *syntax.Nonterm) {
	name := p.lexer.text()
	p.consume(ID)
	for i, val := range p.out.Nonterms {
		if val.Name == name {
			return i + len(p.out.Terminals), val
		}
	}
	p.errorf("%q is not found", name)
	return 0, nil
}

func (p *parser) parseTermRef() int {
	name := p.lexer.text()
	p.consume(TERM)
	for i, val := range p.out.Terminals {
		if val == name {
			return i
		}
	}
	p.errorf("%q is not found", name)
	return 0
}

func (p *parser) parseParamRef() int {
	name := p.lexer.text()
	if !p.consumeIf(NAME) {
		p.consume(ID)
	}
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
	if p.consumeIf('%') {
		// TODO parse %prec
	}
	if p.consumeIf(ARROW) {
		name := p.lexer.text()
		p.consume(ID)
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
	case NAME:
		name := p.lexer.text()
		p.next()
		var kind syntax.ExprKind
		switch p.curr {
		case PLUSEQ:
			kind = syntax.Append
		case '=':
			kind = syntax.Assign
		default:
			p.errorf("wrong assignment")
			return nil
		}
		p.next()
		inner := p.parseOpt()
		return &syntax.Expr{Kind: kind, Name: name, Sub: []*syntax.Expr{inner}}
	case '.':
		p.next()
		name := p.lexer.text()
		p.consume(ID)
		return &syntax.Expr{Kind: syntax.StateMarker, Name: name}
	case CODE:
		code := p.lexer.text()
		p.next()
		return &syntax.Expr{Kind: syntax.Command, Name: code}
	case LOOKAHEAD:
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
	if p.curr == AS {
		p.next()
		// TODO parse as
	}
	if p.curr == '?' {
		p.next()
		inner = &syntax.Expr{Kind: syntax.Optional, Sub: []*syntax.Expr{inner}}
	}
	return inner
}

func (p *parser) parseSymref() *syntax.Expr {
	if p.curr == TERM {
		return &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef()}
	}
	sym, _ := p.parseNontermRef()
	var args []syntax.Arg
	if p.consumeIf('<') {
		args = append(args, p.parseArg())
		for p.consumeIf(',') {
			args = append(args, p.parseArg())
		}
		p.consume('>')
	}
	return &syntax.Expr{Kind: syntax.Reference, Symbol: sym, Args: args}
}

func (p *parser) parseArg() syntax.Arg {
	param := p.parseParamRef()
	p.consume('=')
	val := p.lexer.text()
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
	case TERM, ID:
		ret = p.parseSymref()
	case '(':
		p.next()
		ret = &syntax.Expr{Kind: syntax.Choice}
		ret.Sub = append(ret.Sub, p.parseRule())
		if p.consumeIf(SEPARATOR) {
			sep = &syntax.Expr{Kind: syntax.Sequence}
			for {
				sym := &syntax.Expr{Kind: syntax.Reference, Symbol: p.parseTermRef()}
				sep.Sub = append(sep.Sub, sym)
				if p.curr != TERM {
					break
				}
			}
			p.consume(')')
			if p.curr != '+' && p.curr != '*' {
				p.errorf("qualifier is expected")
			}
			break
		}
		for p.consumeIf('|') {
			ret.Sub = append(ret.Sub, p.parseRule())
		}
		p.consume(')')
	case SET:
		p.next()
		p.consume('(')
		// TODO parse set
		p.consume(')')
	default:
		return nil
	}
	for p.curr == '+' || p.curr == '*' {
		var flags syntax.ListFlags
		if p.curr == '+' {
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
