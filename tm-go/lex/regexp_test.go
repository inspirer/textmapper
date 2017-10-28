package lex

import (
	"bytes"
	"fmt"
	"testing"
	"strings"
)

var parseTests = []struct {
	input string
	want string
}{
	{``, `cat{}`},
	{`a()`, `str{a}`},
	{`(a)`, `str{a}`},
	{`((())a())`, `str{a}`},
	{`a`, `str{a}`},
	{`ab`, `str{ab}`},
	{`+`, `str{+}`},
	{`++`, `str{+}+`},
	{`|+`, `alt{cat{}str{+}}`},
	{`a|+`, `alt{str{a}str{+}}`},
	{`.+`, `cc{\x00-\t\x0b-\U0010ffff}+`},
	{`([.a-z])+`, `cc{\x00-\t\x0b-\U0010ffff}+`},
	{`a.b`, `cat{str{a}cc{\x00-\t\x0b-\U0010ffff}str{b}}`},
	{`ab+`, `cat{str{a}str{b}+}`},
	{`ab?`, `cat{str{a}str{b}?}`},
	{`ab*`, `cat{str{a}str{b}*}`},
	{`{abc}`, `ext{abc}`},
	{`{abc}{5}`, `ext{abc}{5,5}`},
	{`{abc}{5,}`, `ext{abc}{5,-1}`},
	{`{abc}{5,8}`, `ext{abc}{5,8}`},
	{`{abc}{123,543}`, `ext{abc}{123,543}`},
	{`ab{1,3}`, `cat{str{a}str{b}{1,3}}`},
	{`a(b)`, `cat{str{a}str{b}}`},
	{`a(b|c)`, `cat{str{a}alt{str{b}str{c}}}`},
	{`a(b|c)+`, `cat{str{a}alt{str{b}str{c}}+}`},
	{`[]]`, `cc{\]}`},
	{`[^]]`, `cc{\x00-\\\^-\U0010ffff}`},
	{`[\000-\010\012-\025]`, `cc{\x00-\x08\n-\x15}`},
	{`[arz\n-]`, `cc{\n\-arz}`},
	{`[a-z]`, `cc{a-z}`},
	{`[\000-\n\014-\125]`, `cc{\x00-\n\x0c-U}`},
	{`[-\n\014-\125]`, `cc{\n\x0c-U}`},
	{`[-a-zA-Z-]`, `cc{\-A-Za-z}`},

	// Escapes.
	{`\(\)`, `cat{cc{\(}cc{\)}}`},
	{`\a+\f\n\r\t\v`, `cat{cc{\x07}+cc{\x0c}cc{\n}cc{\r}cc{\t}cc{\x0b}}`},
	{`\123\000`, `cat{cc{S}cc{\x00}}`},
	{`\x00\x01`, `cat{cc{\x00}cc{\x01}}`},
	{`\_`, `cc{_}`},

	// Unicode.
	{`\p{Any}+`, `cc{\x00-\U0010ffff}+`},
	{`\P{Any}+`, `cc{}+`},
	{`\p{^Any}+`, `cc{}+`},
	{`\pZ`, `cc{ \u00a0\u1680\u2000-\u200a\u2028-\u2029\u202f\u205f\u3000}`},
	{`\u1234`, `cc{\u1234}`},
	{`\u{1234}a`, `cat{cc{\u1234}str{a}}`},
	{`\u{123}`, `cc{\u0123}`},
	{`\u{aBcD}`, `cc{\uabcd}`},
	{`\U00001234`, `cc{\u1234}`},
	{`\U00012345`, `cc{\U00012345}`},
	{"\u0370", "str{\u0370}"},
	{"\u0370\u0371+", "cat{str{\u0370}str{\u0371}+}"},

	// Errors.
	{"\xfe\xfe", `err{broken regexp: invalid rune}`},
	{`(`, `err{broken regexp: missing closing parenthesis}`},
	{`(a`, `err{broken regexp: missing closing parenthesis}`},
	{`)`, `err{broken regexp: unexpected closing parenthesis}`},
	{`a\`, `err{broken regexp: trailing backslash at end of regular expression}`},
	{`\Q`, `err{broken regexp: invalid escape sequence}`},
	{`(a))`, `err{broken regexp: unexpected closing parenthesis}`},
	{`\p{`, `err{broken regexp: invalid \p{} range}`},
	{`\p{}`, `err{broken regexp: invalid \p{} range}`},
	{`\p{z}`, `err{broken regexp: unknown unicode character class}`},
	{`{1,3}`, `err{broken regexp: unexpected quantifier}`},
	{`{abc}{543,123}`, `err{broken regexp: invalid quantifier}`},
	{`{abc}{,123}`, `err{broken regexp: invalid external regexp reference}`},
	{`{abc}{99999999999999999999}`, `err{broken regexp: cannot parse quantifier}`},
	{`{abc}{1,99999999999999999999}`, `err{broken regexp: cannot parse quantifier}`},
	{`{abc}{1,`, `err{broken regexp: cannot parse quantifier}`},
	{`ab{`, `err{broken regexp: invalid external regexp reference}`},
	{`{`, `err{broken regexp: invalid external regexp reference}`},
	{`[a-z`, `err{broken regexp: missing closing bracket}`},
	{`[\p{L}-z`, `err{broken regexp: missing closing bracket}`},
	{`[a-\p{L}]`, `err{broken regexp: invalid character class range}`},
	{`[z-a]`, `err{broken regexp: invalid character class range}`},
	{`\00`, `err{broken regexp: invalid escape sequence}`},
	{`\u123`, `err{broken regexp: invalid escape sequence}`},
	{`\u{ }`, `err{broken regexp: invalid escape sequence}`},
}

func TestParse(t *testing.T) {
	for _, test := range parseTests {
		var sensitive bool
		input := test.input
		if strings.HasPrefix(input, "(?i)") {
			sensitive = true
			input = strings.TrimPrefix(input, "(?i)")
		}
		re, err := ParseRegex(input, sensitive)
		var got string
		if err != nil {
			got = fmt.Sprintf("err{%v}", err)
		} else {
			got = dump(re)
		}
		if got != test.want {
			t.Errorf("ParseRegex(%v) = %v, want: %v", test.input, got, test.want)
		}
	}
}

func dump(re *Regexp) string {
	var b bytes.Buffer
	dumpRegexp(re, &b)
	return b.String()
}

func dumpRegexp(re *Regexp, b *bytes.Buffer) {
	switch re.op {
	case opLiteral:
		fmt.Fprintf(b, `str{%s}`, re.text)
	case opCharClass:
		fmt.Fprintf(b, "cc{%s}", re.charset)
	case opRepeat:
		dumpRegexp(re.sub[0], b)
		switch {
		case re.min == 0 && re.max == -1:
			b.WriteString("*")
		case re.min == 1 && re.max == -1:
			b.WriteString("+")
		case re.min == 0 && re.max == 1:
			b.WriteString("?")
		default:
			fmt.Fprintf(b, "{%v,%v}", re.min, re.max)
		}
	case opConcat:
		b.WriteString("cat{")
		for _, s := range re.sub {
			dumpRegexp(s, b)
		}
		b.WriteString("}")
	case opAlternate:
		b.WriteString("alt{")
		for _, s := range re.sub {
			dumpRegexp(s, b)
		}
		if len(re.sub) == 0 {
			b.WriteString("<empty>")
		}
		b.WriteString("}")
	case opExternal:
		fmt.Fprintf(b, "ext{%s}", re.text)
	default:
		b.WriteString("unknown")
	}
}
