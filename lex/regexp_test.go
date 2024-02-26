package lex

import (
	"fmt"
	"strings"
	"testing"
)

var parseTests = []struct {
	input string
	want  string
}{
	{``, `cat{}`},
	{`a()`, `str{a}`},
	{`(a)`, `str{a}`},
	{`((())a())`, `str{a}`},
	{`a`, `str{a}`},
	{`ab`, `str{ab}`},
	{`+`, `str{+}`},
	{`++`, `str{+}+`},
	{`|+`, `alt{cat{},str{+}}`},
	{`a|+`, `alt{str{a},str{+}}`},
	{`.+`, `cc{\x00-\t\x0b-\U0010ffff}+`},
	{`{#bytes}.+`, `cc{\x00-\t\x0b-\u00ff}+`}, // just one byte in "scan bytes" mode
	{`([.a-z])+`, `cc{\x00-\t\x0b-\U0010ffff}+`},
	{`a.b`, `cat{str{a}cc{\x00-\t\x0b-\U0010ffff}str{b}}`},
	{`ab+`, `cat{str{a}str{b}+}`},
	{`ab?`, `cat{str{a}str{b}?}`},
	{`ab*`, `cat{str{a}str{b}*}`},
	{`{#bytes}ab*`, `cat{bytes{a}bytes{b}*}`},
	{`{#bytes}αβ+`, `cat{bytes{α}bytes{β}+}`}, // uncode parsed as utf-8 code units
	{`{abc}`, `ext{abc}`},
	{`{abc}{5}`, `ext{abc}{5,5}`},
	{`{abc}{5,}`, `ext{abc}{5,-1}`},
	{`{abc}{5,8}`, `ext{abc}{5,8}`},
	{`{abc}{123,543}`, `ext{abc}{123,543}`},
	{`ab{1,3}`, `cat{str{a}str{b}{1,3}}`},
	{`a(b)`, `cat{str{a}str{b}}`},
	{`a(b|c)`, `cat{str{a}alt{str{b},str{c}}}`},
	{`a(b|c)+`, `cat{str{a}alt{str{b},str{c}}+}`},
	{`[]]`, `cc{\]}`},
	{`[^]]`, `cc{\x00-\\\^-\U0010ffff}`},
	{`[\000-\010\012-\025]`, `cc{\x00-\x08\n-\x15}`},
	{`[arz\n-]`, `cc{\n\-arz}`},
	{`[a-z]`, `cc{a-z}`},
	{`[\000-\n\014-\125]`, `cc{\x00-\n\x0c-U}`},
	{`[-\n\014-\125]`, `cc{\n\x0c-U}`},
	{`[-a-zA-Z-]`, `cc{\-A-Za-z}`},
	{`0o7(_*7)*_+`, `cat{str{0o7}cat{str{_}*str{7}}*str{_}+}`},

	// Set manipulations.
	{`[-[a-z]]`, `cc{}`}, // empty, not negated
	{`[--[a-z]]`, `cc{\-}`},
	{`[A-Z-[D-F]]`, `cc{A-CG-Z}`},
	{`[A-Z-[D]-[EF]]`, `cc{A-CG-Z}`},
	{`[\p{Lu}\xc0-\U0010ffff]`, `cc{A-Z\u00c0-\U0010ffff}`},
	{`[\p{Lu}-[\u0100-\U0010ffff]]`, `cc{A-Z\u00c0-\u00d6\u00d8-\u00de}`},
	{`[\p{L}-\p{Lu}-[\u0100-\U0010ffff]]`, `cc{a-z\u00aa\u00b5\u00ba\u00df-\u00f6\u00f8-\u00ff}`},
	{`[\p{L}-[\u0100-\U0010ffff]-\p{Lu}]`, `cc{a-z\u00aa\u00b5\u00ba\u00df-\u00f6\u00f8-\u00ff}`},
	{`[\p{Any}]`, `cc{\x00-\U0010ffff}`},
	{`[\p{Any}-[\x00\x01\x02]]`, `cc{\x03-\U0010ffff}`},
	{`[\p{Any}-[\x00\x01\x02]-[\x80-\U0010ffff]-\p{Lu}]`, `cc{\x03-\@\[-\u007f}`},
	{`[\p{Any}-\p{L}-[\u0100-\U0010ffff]]`, `cc{\x00-\@\[-\` + "`" + `\{-\u00a9\u00ab-\u00b4\u00b6-\u00b9\u00bb-\u00bf\u00d7\u00f7}`},

	// Case folding.
	{`(?i)abC`, `cat{cc{Aa}cc{Bb}cc{Cc}}`},
	{`(?i)[a-en-q]`, `cc{A-EN-Qa-en-q}`},
	{`(?i)\u0041`, `cc{Aa}`},
	{`(?i)\101b`, `cat{cc{Aa}cc{Bb}}`},
	{`(?i)[^b-e]`, `cc{\x00-AF-af-\U0010ffff}`},
	{`(?i)+[^]]`, `cat{str{+}cc{\x00-\\\^-\U0010ffff}}`},
	{`abc((?i)ab)`, `cat{str{abc}cat{cc{Aa}cc{Bb}}}`},
	{`abc(?i:ab)`, `cat{str{abc}cat{cc{Aa}cc{Bb}}}`},
	{`abc(((?i:ab)))`, `cat{str{abc}cat{cc{Aa}cc{Bb}}}`},
	{`abc(((?:ab)))`, `cat{str{abc}str{ab}}`},
	{`abc(?i)`, `str{abc}`},
	{`a(?i:a)a`, `cat{str{a}cc{Aa}str{a}}`},
	{`(?i)a(?-i:a)a`, `cat{cc{Aa}str{a}cc{Aa}}`},
	{`(?i)a(?i-:a(?i)b)a`, `cat{cc{Aa}cat{str{a}cc{Bb}}cc{Aa}}`},
	{`(?i)a(?i-)a(?i)ba`, `cat{cc{Aa}str{a}cc{Bb}cc{Aa}}`},
	{`{#fold}a(?i-)a(?i)ba`, `cat{cc{Aa}str{a}cc{Bb}cc{Aa}}`},

	// Escapes.
	{`\(\)`, `cat{cc{\(}cc{\)}}`},
	{`\a+\f\n\r\t\v`, `cat{cc{\x07}+cc{\x0c}cc{\n}cc{\r}cc{\t}cc{\x0b}}`},
	{`\123\000`, `cat{cc{S}cc{\x00}}`},
	{`\x00\x01`, `cat{cc{\x00}cc{\x01}}`},
	{`\_`, `cc{_}`},
	{`\Q+?-\Eabc`, `cat{str{+?-}str{abc}}`},
	{`\Q+abc+`, `str{+abc+}`},
	{`+\Q+*+\E+`, `cat{str{+}str{+*+}+}`},
	{`\d\D`, `cat{cc{0-9}cc{\x00-\/\:-\U0010ffff}}`},
	{`\w`, `cc{0-9A-Z_a-z}`},
	{`[^\W]`, `cc{0-9A-Z_a-z}`},
	{`[\W]`, "cc{\\x00-\\/\\:-\\@\\[-\\^\\`\\{-\\U0010ffff}"},
	{`[\s]`, `cc{\t-\r }`},
	{`[^\s]`, `cc{\x00-\x08\x0e-\x1f\!-\U0010ffff}`},
	{`\S`, `cc{\x00-\x08\x0e-\x1f\!-\U0010ffff}`},
	{`[\S]`, `cc{\x00-\x08\x0e-\x1f\!-\U0010ffff}`},
	{`+\+`, `cat{str{+}cc{\+}}`},

	// Unicode.
	{`\p{Any}+\pZ`, `cat{cc{\x00-\U0010ffff}+cc{ \u00a0\u1680\u2000-\u200a\u2028-\u2029\u202f\u205f\u3000}}`},
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
	{`(?i)γ`, `cc{\u0393\u03b3}`},
	{`{#bytes}(?i)γ`, `bytes{γ}`}, // No case folding for non-ASCII in bytes mode.

	// Errors.
	{"\xfe\xfe", "err{invalid rune}: «\xfe»\xfe"},
	{"\\Q\xfe\xfe\\E", "err{invalid rune}: \\Q«\xfe»\xfe\\E"},
	{"\\Q\u0370\xfe\xfe\\E", "err{invalid rune}: \\Q\u0370«\xfe»\xfe\\E"},
	{`(`, `err{missing closing parenthesis}: («»`},
	{`(a`, `err{missing closing parenthesis}: (a«»`},
	{`)`, `err{unexpected closing parenthesis}: «)»`},
	{`a\`, `err{trailing backslash at end of regular expression}: a«\»`},
	{`\T`, `err{invalid escape sequence}: «\T»`},
	{`(a))`, `err{unexpected closing parenthesis}: (a)«)»`},
	{`\p{`, `err{invalid \p{} range}: «\p{»`},
	{`\p{}`, `err{invalid \p{} range}: «\p{}»`},
	{`{#bytes}\p{Lu}`, `err{unknown unicode character class}: «\p{Lu}»`}, // No Lu in bytes mode.
	{`\p{z}`, `err{unknown unicode character class}: «\p{z}»`},
	{`{1,3}`, `err{unexpected quantifier}: «{»1,3}`},
	{`{abc}{543,123}`, `err{invalid quantifier}: {abc}«{543,123}»`},
	{`{abc}{,123}`, `err{invalid external regexp reference}: {abc}«{,»123}`},
	{`{abc}{99999999999999999999}`, `err{cannot parse quantifier}: {abc}{«99999999999999999999»}`},
	{`{abc}{1,99999999999999999999}`, `err{cannot parse quantifier}: {abc}{1,«99999999999999999999»}`},
	{`{abc}{1,`, `err{cannot parse quantifier}: {abc}{«1,»`},
	{`ab{`, `err{invalid external regexp reference}: ab«{»`},
	{`{`, `err{invalid external regexp reference}: «{»`},
	{`[a-z`, `err{missing closing bracket}: «[a-z»`},
	{`[\p{L}-z`, `err{missing closing bracket}: «[\p{L}-z»`},
	{`[qa-\p{L}]`, `err{invalid character class range}: [q«a-\p{L}»]`},
	{`[z-a]`, `err{invalid character class range}: [«z-a»]`},
	{`\00`, `err{invalid escape sequence}: «\00»`},
	{`\00a`, `err{invalid escape sequence}: «\00a»`},
	{`\400`, `err{invalid escape sequence (max = \377)}: «\400»`},
	{`\u123`, `err{invalid escape sequence}: «\u123»`},
	{`\u{ }`, `err{invalid escape sequence}: \u{« »}`},
	{`\U0010ffff`, `cc{\U0010ffff}`},
	{`\U00110000`, `err{invalid escape sequence (exceeds unicode.MaxRune)}: «\U00110000»`},
	{`{#bytes}\u0abc`, `bytes{઼}`},
	{`{#bytes}[\u0abc]`, `err{invalid escape sequence (exceeds \uff)}: [«\u0abc»]`},
	{`{#bytes}\u00ff`, "bytes{\u00ff}"},
	{`{#bytes}\u0100`, `bytes{Ā}`},
	{`{#bytes}[\u0100]`, `err{invalid escape sequence (exceeds \uff)}: [«\u0100»]`},
	{`abc(?`, `err{unknown perl flags}: abc(?«»`},
	{`abc(?ie`, `err{unknown perl flags}: abc(?i«e»`},
	{`\u00a0`, `cc{\u00a0}`},
	{`{#bytes}\u00a0`, "bytes{\u00a0}"},
	{`[\u03b1-\u03b3]`, `cc{\u03b1-\u03b3}`}, // ok
	{`{#bytes}[\u03b1-\u03b3]`, `err{invalid escape sequence (exceeds \uff)}: [«\u03b1»-\u03b3]`},
	{`{#bytes}[α-\u03b3]`, `err{invalid character \u3b1 (exceeds \uff)}: [«α»-\u03b3]`},
	{`{#bytes}[0-\u03b3]`, `err{invalid escape sequence (exceeds \uff)}: [0-«\u03b3»]`},
	{`{#bytes}[α-γ]`, `err{invalid character \u3b1 (exceeds \uff)}: [«α»-γ]`},
	{`{#bytes}[0-γ]`, `err{invalid character \u3b3 (exceeds \uff)}: [0-«γ»]`},
}

func TestParse(t *testing.T) {
	for _, test := range parseTests {
		input := test.input
		var opts CharsetOptions
		input, opts.Fold = strings.CutPrefix(input, "{#fold}")
		input, opts.ScanBytes = strings.CutPrefix(input, "{#bytes}")
		re, err := ParseRegexp(input, opts)
		var got string
		if err != nil {
			err := err.(ParseError)
			if err.Offset > err.EndOffset || err.Offset < 0 || err.EndOffset > len(input) {
				t.Errorf("invalid error offsets for %v: %v, %v (%v)", input, err.Offset, err.EndOffset, err.Msg)
				continue
			}
			got = fmt.Sprintf("err{%v}: %v«%v»%v", err.Msg, input[:err.Offset], input[err.Offset:err.EndOffset], input[err.EndOffset:])
		} else {
			got = dump(re)
		}
		if got != test.want {
			t.Errorf("ParseRegex(%v) = %v, want: %v", test.input, got, test.want)
		}
	}
}

var stringTests = []struct {
	input string
	want  string
}{
	{``, ``},
	{`a`, `a`},
	{`ab`, `ab`},
	{`a()`, `a`},
	{`(a)`, `a`},
	{`((())a())`, `a`},
	{`+`, `+`},
	{`++`, `++`},
	{`\+\+`, `\+\+`},
	{`|+`, `|+`},
	{`a|+`, `a|+`},
	{`.+`, `[\x00-\t\x0b-\U0010ffff]+`},
	{`([.a-z])+`, `[\x00-\t\x0b-\U0010ffff]+`},
	{`a.b`, `a[\x00-\t\x0b-\U0010ffff]b`},
	{`ab+`, `ab+`},
	{`ab?`, `ab?`},
	{`ab*`, `ab*`},
	{`{abc}`, `{abc}`},
	{`{abc}{5}`, `{abc}{5}`},
	{`{abc}{5,}`, `{abc}{5,}`},
	{`{abc}{5,8}`, `{abc}{5,8}`},
	{`{abc}{123,543}`, `{abc}{123,543}`},
	{`ab{1,3}`, `ab{1,3}`},
	{`a(b)`, `ab`},
	{`a(b|c)`, `a(b|c)`},
	{`a(b|c)+`, `a(b|c)+`},
	{`[]]`, `\]`},
	{`[^]]`, `[\x00-\\\^-\U0010ffff]`},
	{`[arz\n-]`, `[\n\-arz]`},
	{`[a-z]`, `[a-z]`},
	{`[\000-\n\014-\125]`, `[\x00-\n\x0c-U]`},
	{`[-\n\014-\125]`, `[\n\x0c-U]`},
	{`[-a-zA-Z-]`, `[\-A-Za-z]`},

	// Case folding.
	{`(?i)abC`, `[Aa][Bb][Cc]`},
	{`(?i)[a-en-q]`, `[A-EN-Qa-en-q]`},
	{`{#fold}[a-en-q]`, `[A-EN-Qa-en-q]`},
	{`{#fold}(?-i)[a-en-q]`, `[a-en-q]`},

	// Escapes.
	{`\d\D`, `[0-9][\x00-\/\:-\U0010ffff]`},
	{`\w+`, `[0-9A-Z_a-z]+`},
	{`\S`, `[\x00-\x08\x0e-\x1f\!-\U0010ffff]`},

	// Parentheses.
	{`a+|(b|cd|)`, `a+|(b|cd|)`},
}

func TestString(t *testing.T) {
	for _, test := range stringTests {
		input := test.input
		var opts CharsetOptions
		input, opts.Fold = strings.CutPrefix(input, "{#fold}")
		re, err := ParseRegexp(input, opts)
		if err != nil {
			t.Errorf("ParseRegexp(%v) failed with %v", input, err)
			continue
		}
		if got := re.String(); test.want != got {
			t.Errorf("ParseRegexp(%v).String() = %v, want: %v", input, got, test.want)
		}
	}
}

var constTests = []struct {
	input string
	want  string
}{
	{`a`, `a`},
	{`ab`, `ab`},
	{`a()`, `a`},
	{`a(b)`, `ab`},
	{`(a)`, `a`},
	{`((())a())`, `a`},
	{`+`, `+`},
	{`++`, ``},
	{`\+\+`, `++`},
	{`{#bytes}\+\+`, `++`},
	{`{#fold}\+\+`, `++`},
	{`{#fold}aa`, ``},
}

func TestConstants(t *testing.T) {
	for _, test := range constTests {
		input := test.input
		var opts CharsetOptions
		input, opts.Fold = strings.CutPrefix(input, "{#fold}")
		input, opts.ScanBytes = strings.CutPrefix(input, "{#bytes}")
		re, err := ParseRegexp(input, opts)
		if err != nil {
			t.Errorf("ParseRegexp(%v) failed with %v", input, err)
			continue
		}
		got, ok := re.Constant()
		if (got != "") != ok {
			t.Errorf("ParseRegexp(%v).Constant() returned %v with ok=%v", input, got, ok)
		}
		if test.want != got {
			t.Errorf("ParseRegexp(%v).Constant() = %q, %v; want: %q", input, got, ok, test.want)
		}
	}
}

// All positions of character classes, literals, or external references are marked with underscores.
var offsetTests = []string{
	"_abcd123",
	"_abc\\Q_ffffff\\E",
	"_A_B+",
	"(_AB)+",
	"(_{foo}+)",
	"(?i)_a_b_c",
	"_[a-zA-Z]_[a-zA-Z0-9]+",
	"_abc_\\p{L}_\\w_.",
	"_[^abc]{1,2}",
	"_\\012_5555",
	"{#bytes}_A_B+",
}

func TestOffsets(t *testing.T) {
	for _, input := range offsetTests {
		var opts CharsetOptions
		input, opts.ScanBytes = strings.CutPrefix(input, "{#bytes}")
		want := input
		input = strings.ReplaceAll(input, "_", "")
		re, err := ParseRegexp(input, opts)
		if err != nil {
			t.Errorf("ParseRegexp(%v) failed with %v", input, err)
			continue
		}
		var sb strings.Builder
		var flush int
		traverse(re, func(re *Regexp) {
			switch re.op {
			case opExternal, opLiteral, opBytesLiteral, opCharClass:
				sb.WriteString(input[flush:re.offset])
				flush = re.offset
				sb.WriteByte('_')
			}
		})
		sb.WriteString(input[flush:])
		if got := sb.String(); got != want {
			t.Errorf("offsets(%v) = %v, want: %v", input, got, want)
		}
	}
}

func traverse(re *Regexp, consume func(re *Regexp)) {
	consume(re)
	for _, sub := range re.sub {
		traverse(sub, consume)
	}
}

func dump(re *Regexp) string {
	var b strings.Builder
	dumpRegexp(re, &b)
	return b.String()
}

func dumpRegexp(re *Regexp, b *strings.Builder) {
	switch re.op {
	case opLiteral:
		fmt.Fprintf(b, `str{%s}`, re.text)
	case opBytesLiteral:
		fmt.Fprintf(b, `bytes{%s}`, re.text)
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
		for i, s := range re.sub {
			if i > 0 {
				b.WriteByte(',')
			}
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
