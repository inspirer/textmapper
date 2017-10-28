package lex

import (
	"fmt"
	"strconv"
	"strings"
	"testing"
	"unicode"
)

var charsetTests = []struct {
	in   string
	want string
}{
	{``, ``},
	{` `, ` `},
	{`-`, `\-`},
	{`\t-`, `\t\-`},
	{`-\t`, `\t\-`},
	{`\x00`, `\x00`},
	{`\ufeff`, `\ufeff`},
	{`\U0010FFFF`, `\U0010ffff`},
	{`90`, `09`},
	{`r-t`, `r-t`},
	{`a-zb-c`, `a-z`},
	{`b-ca-z`, `a-z`},
	{`b-cb-c`, `b-c`},
	{`b-db-c`, `b-d`},
	{`b-cb-d`, `b-d`},
	{`a-be-fy-z`, `a-be-fy-z`},
	{`e-fy-za-b`, `a-be-fy-z`},
	{`c-da-be-f`, `a-f`},
	{`\u1333\u1334`, `\u1333-\u1334`},
	{`\n\t\u1333-\U00101333`, `\t-\n\u1333-\U00101333`},
	{`\a\b\n\r\f\t\v`, `\x07-\r`},
	{`\x00-\U0010ffff`, `\x00-\U0010ffff`},

	// Inverted.
	{`^`, `\x00-\U0010ffff`},
	{`^\n`, `\x00-\t\x0b-\U0010ffff`},
	{`^\x00-\x1f0-9`, ` -\/\:-\U0010ffff`},
	{`^\x20-\U0010ffff`, `\x00-\x1f`},
	{`^\x00-\U0010ffff`, ``},

	// Invalid ranges.
	{`\MaxRune+1`, `\ufffd`},
	{`^\MaxRune+1`, `\x00-\U0010ffff`},
}

func TestNewCharset(t *testing.T) {
	for _, test := range charsetTests {
		s := test.in
		inv := strings.HasPrefix(s, "^")
		if inv {
			s = s[1:]
		}
		in, err := parseRanges(s)
		if err != nil {
			t.Errorf("parseRanges() failed with %v", err)
		}
		out := newCharset(in)
		if inv {
			out.invert()
		}
		if got := out.String(); got != test.want {
			t.Errorf("newCharset(%#q) = %#q, want: %#q", test.in, got, test.want)
		}
	}
}

var intersectTests = []struct {
	a    string
	b    string
	want string
}{
	{``, ``, ``},
	{`a-f`, `f-z`, `f`},
	{`golang`, `a-z`, `agln-o`},
	{`a-zA-Z`, `a-zA-Z`, `A-Za-z`},
	{`a-x`, `fqu-z`, `fqu-x`},
	{`3-8`, `0-38-9`, `38`},
	{`3-8`, `0-46-7`, `3-46-7`},
	{`a-z\u1000-\u1100\u2000-\u2100\u3000-\u3100`, `\u1050-\u3050`, `\u1050-\u1100\u2000-\u2100\u3000-\u3050`},
	{`a-z\u1000-\u1100-`, `\x00-\U0010ffff`, `\-a-z\u1000-\u1100`},
}

func TestIntersect(t *testing.T) {
	for _, test := range intersectTests {
		a, err := parseRanges(test.a)
		if err != nil {
			t.Errorf("parseRanges() failed with %v", err)
		}
		b, err := parseRanges(test.b)
		if err != nil {
			t.Errorf("parseRanges() failed with %v", err)
		}
		out := intersect(newCharset(a), newCharset(b))
		if got := out.String(); got != test.want {
			t.Errorf("intersect(%#q,%#q) = %#q, want: %#q", test.a, test.b, got, test.want)
			continue
		}

		out = intersect(newCharset(b), newCharset(a))
		if got := out.String(); got != test.want {
			t.Errorf("intersect(%#q,%#q) = %#q, want: %#q", test.b, test.a, got, test.want)
		}
	}
}

var subtractTests = []struct {
	a    string
	b    string
	want string
}{
	{``, ``, ``},
	{`a-z`, `0-9`, `a-z`},
	{`d-x`, `az`, `d-x`},
	{`a-z`, `f`, `a-eg-z`},
	{`a-z`, `bf`, `ac-eg-z`},
	{`a-z`, `bfz`, `ac-eg-y`},
	{`a-dx-z`, `x-z`, `a-d`},
	{`a-dx-z`, `a-d`, `x-z`},
	{`a-dx-z`, `c-y`, `a-bz`},
	{`a-dx-z`, `c-\u1000`, `a-b`},
	{`\x80-\x88\x90-\x98\xa0-\xa8\xb0-\xb8`, `\x85-\x95\xa5-\xb8`, `\u0080-\u0084\u0096-\u0098\u00a0-\u00a4`},
}

func TestSubtract(t *testing.T) {
	for _, test := range subtractTests {
		a, err := parseRanges(test.a)
		if err != nil {
			t.Errorf("parseRanges() failed with %v", err)
		}
		b, err := parseRanges(test.b)
		if err != nil {
			t.Errorf("parseRanges() failed with %v", err)
		}

		out := newCharset(a)
		out.subtract(newCharset(b))
		if got := out.String(); got != test.want {
			t.Errorf("(%#q).subtract(%#q) = %#q, want: %#q", test.a, test.b, got, test.want)
		}
	}
}

var unicodeTests = []struct {
	name      string
	sensitive bool
	intersect charset
	want      string
}{
	{"Ll", true, charset{0, 0x7f}, `A-Za-z`},
	{"Ll", false, charset{0, 0x7f}, `a-z`},
	{"N", false, charset{0, 0x7f}, `0-9`},
	{"Pc", true, charset{0, 0x7f}, `_`},
	{"", false, nil, `err: unknown unicode character class`},
	{"Lower", true, nil, `err: unknown unicode character class`},
	{"Any", true, charset{0, unicode.MaxRune}, `\x00-\U0010ffff`},
	{"Greek", false, charset{0, 0x380}, `\u0370-\u0373\u0375-\u0377\u037a-\u037d\u037f`},
}

func TestUnicode(t *testing.T) {
	for _, test := range unicodeTests {
		r, err := appendNamedSet(nil, test.name, test.sensitive)
		var got string
		if err != nil {
			got = "err: " + err.Error()
		} else {
			set := intersect(newCharset(r), test.intersect)
			got = set.String()
		}
		if got != test.want {
			t.Errorf("\\p{%v} (case:%v) = %#q, want: %#q", test.name, test.sensitive, got, test.want)
		}
	}
}

func parseRanges(s string) ([]rune, error) {
	if s == `\MaxRune+1` {
		return []rune{unicode.MaxRune + 1, unicode.MaxRune + 1}, nil
	}

	var ranges []rune
	for len(s) > 0 {
		lo, _, tail, err := strconv.UnquoteChar(s, 0)
		if err != nil {
			return nil, fmt.Errorf("cannot unquote %q: %v", s, err)
		}
		s = tail
		hi := lo
		if len(s) >= 2 && s[0] == '-' {
			hi, _, tail, err = strconv.UnquoteChar(s[1:], 0)
			if err != nil {
				return nil, fmt.Errorf("cannot unquote %q: %v", s, err)
			}
			s = tail
		}
		ranges = appendRange(ranges, lo, hi)
	}
	return ranges, nil
}
