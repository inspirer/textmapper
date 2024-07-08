package gen

import (
	"fmt"
	"log"
	"math/rand"
	"strconv"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/grammar"
	"github.com/inspirer/textmapper/status"
	"github.com/inspirer/textmapper/syntax"
	"github.com/inspirer/textmapper/util/diff"
)

func TestHashing(t *testing.T) {
	tests := []struct {
		input string
		want  string
	}{
		{"", "0"},
		{" ", "0x20"},
		{"__", "0xbe0"},
		{"shift", "0x6856c82"},
		{"inline", "0xb96da299"},
		{"verylongstring", "0x1200fb43"},
	}

	for _, tc := range tests {
		if got := hex(stringHash(tc.input)); got != tc.want {
			t.Errorf("stringHash(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}

func TestBits(t *testing.T) {
	tests := []struct {
		input []int
		want  int
	}{
		{[]int{}, 8},
		{[]int{-128}, 8},
		{[]int{-128, 128}, 16},
		{[]int{65536}, 32},
		{[]int{1 << 31}, 32},
	}

	for _, tc := range tests {
		if got := bitsPerElement(tc.input); got != tc.want {
			t.Errorf("bitsPerElement(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}

func TestIntArray(t *testing.T) {
	tests := []struct {
		input []int
		width int
		want  string
	}{
		{[]int{0}, 0, "\n  0,\n"},
		{[]int{-128, 12}, 12, "\n  -128, 12,\n"},
		{[]int{-128, 128}, 12, "\n  -128,\n  128,\n"},
		{[]int{1, 2, 3, 42, 5, 6, 77, 888, 9, 10, 11}, 13, "\n  1, 2, 3,\n  42, 5, 6,\n  77, 888,\n  9, 10, 11,\n"},
		{[]int{1, 2, 3}, 2, "\n  1,\n  2,\n  3,\n"}, // not enough width
	}

	for _, tc := range tests {
		if got := intArray(tc.input, "  ", tc.width); got != tc.want {
			t.Errorf("intArray(%v) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func TestIntArrayColumns(t *testing.T) {
	tests := []struct {
		input []int
		width int
		want  string
	}{
		{[]int{0}, 0, "\n       0,\n"},
		{[]int{0}, 5, "\n       0,\n"},
		{[]int{-128, 12}, 12, "\n    -128,    12,\n"},
		{[]int{-128, 128}, 1, "\n    -128,\n     128,\n"},
		{[]int{-128, 128}, 2, "\n    -128,   128,\n"},
		{[]int{1, 2, 3, 42, 5, 6, 77, 888, 9, 10, 11}, 5, "\n       1,     2,     3,    42,     5,\n       6,    77,   888,     9,    10,\n      11,\n"},
		{[]int{1, 2, 3, 42, 5, 6, 77, 1234567, 9, 10}, 5, "\n       1,     2,     3,    42,     5,\n       6,    77, 1234567,     9,    10,\n"},
	}

	for _, tc := range tests {
		if got := intArrayColumns(tc.input, "  ", tc.width); got != tc.want {
			t.Errorf("intArrayColumns(%v) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func TestStringSwitch(t *testing.T) {
	swtch := asStringSwitch(map[string]int{
		"abc":  1,
		"def":  2,
		"a":    3,
		"z":    4,
		"abba": 5,
		"e":    6,
		"y":    7,
	})
	const want = "/8=>[0=>[(0x2d9420,abba)=>5] 1=>[(0x61,a)=>3 (0x79,y)=>7] 2=>[(0x17862,abc)=>1 (0x7a,z)=>4] 5=>[(0x18405,def)=>2 (0x65,e)=>6]]"
	if got := switchString(swtch); got != want {
		t.Errorf("stringSwitch() = %v, want: %v", got, want)
	}
}

func switchString(s stringSwitch) string {
	var buf strings.Builder
	fmt.Fprintf(&buf, "/%v=>[", s.Size)
	for i, c := range s.Cases {
		if i > 0 {
			buf.WriteRune(' ')
		}
		fmt.Fprintf(&buf, "%v=>[", hex(c.Value))
		for i, s := range c.Subcases {
			if i > 0 {
				buf.WriteRune(' ')
			}
			fmt.Fprintf(&buf, "(%v,%v)=>%v", hex(s.Hash), s.Str, s.Action)
		}
		buf.WriteRune(']')
	}
	buf.WriteRune(']')
	return buf.String()
}

func BenchmarkIntArray(b *testing.B) {
	r := rand.New(rand.NewSource(42))
	var data [8192]int
	for i := 0; i < len(data); i++ {
		data[i] = r.Intn(1<<16) - 1<<14
	}
	str := intArray(data[:], "  ", 80)
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		intArray(data[:], "  ", 80)
	}
	b.SetBytes(int64(len(str)))
}

func TestParserAction(t *testing.T) {
	tests := []struct {
		input string
		args  *grammar.ActionVars
		want  string
	}{
		{"abc", vars(), "abc"},
		{"($$)", vars(), "(lhs.value)"},
		{"$$ = ${left()}", vars("%abc"), "nn, _ := lhs.value.(abc)\nlhs.value = nn"},

		{"abc $foo ", vars("foo"), "abc nil "},
		{"abc $foo-a ", vars("foo-a:0"), "abc stack[len(stack)-1].value "},
		{"abc $foo-b ", vars("foo-b:0:bar"), "nn0, _ := stack[len(stack)-1].value.(bar)\nabc nn0 "},
		{"$foo $foo", vars("foo:0:bar"), "nn0, _ := stack[len(stack)-1].value.(bar)\nnn0 nn0"},
		{"abc ${foo} ", vars("foo"), "abc nil "},
		{"abc ${foo} ", vars("foo:0"), "abc stack[len(stack)-1].value "},
		{"abc ${foo} ", vars("foo:0:bar"), "nn0, _ := stack[len(stack)-1].value.(bar)\nabc nn0 "},

		{"$a + ${last()}", vars("a:0", "b", "c:1", "d"), "stack[len(stack)-2].value + stack[len(stack)-1].value"},
		{"${first()} + ${left()}", vars("a:0", "b", "c:1", "d"), "stack[len(stack)-2].value + lhs.value"},
		{"${first()} + ${left()}", vars("a:1:bar", "b", "c", "d"), "nn0, _ := stack[len(stack)-1].value.(bar)\nnn0 + lhs.value"},

		{"${left().sym}", vars("a:0", "b", "c:1", "d:2"), "(&lhs.sym)"},
		{"${left().offset}", vars("a:0", "b", "c:1", "d:2"), "lhs.sym.offset"},
		{"${left().endoffset}", vars("a:0", "b", "c:1", "d:2"), "lhs.sym.endoffset"},
		{"${last().sym}", vars("a:0", "b", "c:1", "d:2"), "(&stack[len(stack)-1].sym)"},
		{"${last().offset}", vars("a:0", "b", "c:1", "d:2"), "stack[len(stack)-1].sym.offset"},
		{"${last().endoffset}", vars("a:0", "b", "c:1", "d:2"), "stack[len(stack)-1].sym.endoffset"},
	}

	for _, tc := range tests {
		got, err := goParserAction(tc.input, tc.args, node(tc.input))
		if err != nil {
			t.Errorf("parserAction(%v, %v) failed with %v", tc.input, tc.args, err)
			continue
		}
		if diff := diff.LineDiff(tc.want, got); diff != "" {
			t.Errorf("parserAction(%v, %v) failed with diff:\n--- want\n+++ got\n%v", tc.input, tc.args, diff)
		}
	}
}

func TestCcParserAction(t *testing.T) {
	tests := []struct {
		input string
		args  *grammar.ActionVars
		want  string
	}{
		{"abc", varsOneBased(), "abc"},
		{"$$ = $1", varsOneBased("%node", "a:0:expr"), "lhs.value.node = rhs[0].value.expr"},
		{"$$ = @$ @1", varsOneBased("%node", "a:0:expr"), "lhs.value.node = lhs.sym.location rhs[0].sym.location"},
	}

	for _, tc := range tests {
		got, err := ccParserAction(tc.input, tc.args, node(tc.input))
		if err != nil {
			t.Errorf("parserAction(%v, %v) failed with %v", tc.input, tc.args, err)
			continue
		}
		if diff := diff.LineDiff(tc.want, got); diff != "" {
			t.Errorf("parserAction(%v, %v) failed with diff:\n--- want\n+++ got\n%v", tc.input, tc.args, diff)
		}
	}
}

func varsOneBased(list ...string) *grammar.ActionVars {
	return varsWithOffset(false, list...)
}

func vars(list ...string) *grammar.ActionVars {
	return varsWithOffset(true, list...)
}

func varsWithOffset(zeroBased bool, list ...string) *grammar.ActionVars {
	ret := &grammar.ActionVars{
		CmdArgs: syntax.CmdArgs{
			MaxPos: 1 + len(list),
			Names:  make(map[string]int),
		},
		Remap: make(map[int]int),
	}
	for i, descr := range list {
		if strings.HasPrefix(descr, "%") {
			ret.LHSType = descr[1:]
			continue
		}
		name, num, mapped := strings.Cut(descr, ":")
		if name != "" {
			ret.Names[name] = i
		}
		if !mapped {
			continue
		}
		num, tp, _ := strings.Cut(num, ":")
		target, err := strconv.Atoi(num)
		if err != nil {
			log.Fatalf("cannot parse %q as a number in %q", num, descr)
		}
		ret.Types = append(ret.Types, tp)
		index := i
		if !zeroBased {
			index++
		}
		ret.Remap[index] = target
	}
	return ret
}

type node string

func (n node) SourceRange() status.SourceRange {
	return status.SourceRange{Filename: string(n)}
}

func TestLastID(t *testing.T) {
	tests := []struct {
		input string
		want  string
	}{
		{"a1 *B1", "B1"},
		{"int* foo_bar  ", "foo_bar"},
		{"foo_bar1  ", "foo_bar1"},
	}
	for _, tc := range tests {
		if got := lastID(tc.input); got != tc.want {
			t.Errorf("lastID(%v) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}
