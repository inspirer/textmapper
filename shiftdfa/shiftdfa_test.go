package shiftdfa

import (
	"fmt"
	"strings"
	"testing"
	"unicode/utf8"
)

type input struct {
	text       string
	wantAction int
}

var tests = []struct {
	rules  []Rule
	testOn []input
	opts   Options
}{
	{
		rules: []Rule{
			{Pattern: `\/\*{commentChars}\*\/`, Token: 1},
			{Pattern: `\/\*{commentChars}`, Token: 0 /*incomplete multiline comment*/},
			{Pattern: `\/\/[^\n\r]*`, Token: 2},
		},
		opts: Options{
			Patterns: map[string]string{
				"commentChars": `([^*]|\*+[^*\/])*\**`,
			},
		},
		testOn: []input{
			{`«/* abc */»`, 1},
			{`«/* abc */» foo`, 1},
			{`«/*** abc ***/» foo`, 1},
			{`«/*** a / bc ***/» foo`, 1},
			{`«/*** a /** bc ***/» foo`, 1},
			{`«//*** a /** bc ***/ foo»`, 2},
			{`«//*** a /** bc ***/ foo»
			     bar`, 2},
			{`«»`, 0},   // invalid token
			{`«»ab`, 0}, // invalid token
		},
	},
	{
		rules: []Rule{
			{Pattern: `[a-z]+(-[a-z]+)*`, Token: 1},
			{Pattern: `[a-z]+(-[a-z]+)*-`, Token: 0}, // invalid token (manual)
		},
		testOn: []input{
			{`«abc» b`, 1},
			{`«ab-c» b`, 1},
			{`«ab-» b`, 0}, // invalid token
		},
	},
	{
		rules: []Rule{
			{Pattern: `keyword`, Token: 1},
		},
		testOn: []input{
			{`«keyword»`, 1},
			{`«keyword» `, 1},
			{`«keywor»D `, 0}, // invalid token
		},
	},
	{
		// Precedence resolution.
		rules: []Rule{
			{Pattern: `keyword`, Token: 1},
			{Pattern: `[a-z]+`, Token: 2, Precedence: -1},
			{Pattern: `[a-zA-Z]+`, Token: 31, Precedence: -2},
		},
		testOn: []input{
			{`«abc»`, 2},
			{`«abc» def`, 2},
			{`«keywor» def`, 2},
			{`«keyword» def`, 1},
			{`«keyword»`, 1},
			{`«keyworddef»!`, 2},
			{`«keywordDef»!`, 31},
		},
	},
}

func TestDFA(t *testing.T) {
	repl := strings.NewReplacer("«", "", "»", "")
	for _, tc := range tests {
		dfa, err := Compile(tc.rules, tc.opts)
		if err != nil {
			t.Errorf("Compile(%v) failed with %v", tc.rules, err)
			continue
		}

		for _, inp := range tc.testOn {
			text := repl.Replace(inp.text)
			size, token := dfa.Scan(text)
			got := fmt.Sprintf("«%v»%v", text[:size], text[size:])
			if got != inp.text {
				t.Errorf("dfa.Scan(%v).Token = %v, want: %v", text, got, inp.text)
			}
			if int(token) != inp.wantAction {
				t.Errorf("dfa.Scan(%v).Action = %v, want: %v", inp.text, token, inp.wantAction)
			}
		}
	}
}

func BenchmarkDFA(b *testing.B) {
	dfa := MustCompile([]Rule{
		{Pattern: `keyword`, Token: 1},
		{Pattern: `[a-z]+`, Token: 2, Precedence: -1},
		{Pattern: `[a-zA-Z]+`, Token: 3, Precedence: -2},
	}, Options{})

	str := strings.Repeat("aB", 4096)
	size, act := dfa.Scan(str)
	if size != len(str) || act != 3 {
		b.Fatalf("Scan() = %v, %v; want: %v, %v", size, act, len(str), 2)
	}

	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		dfa.Scan(str)
	}
	b.SetBytes(int64(len(str)))
}

func scanWord(input string) (size int, token uint8) {
	if len(input) == 0 {
		return 0, 0
	}
	c := input[0]
	if c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' {
		for i := 1; i < len(input); i++ {
			c := input[i]
			if !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_') {
				return i, 1
			}
		}
		return len(input), 1
	}

	for i := 1; i < len(input); i++ {
		c := input[i]
		if c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' {
			return i, 1
		}
	}
	return len(input), 1
}

var testDFA = *MustCompile([]Rule{
	{Pattern: `[a-zA-Z0-9_]+`, Token: 1},
	{Pattern: `[^a-zA-Z0-9_]+`, Token: 2},
}, Options{})

func scanWordDFA(input string) (size int, token uint8) {
	var state uint64
	var i int
	for ; i < len(input) && (state&1) == 0; i++ {
		row := testDFA.table[input[i]]
		state = row >> (state & 63)
	}
	if (state & 1) == 0 {
		return len(input), testDFA.onEoi[(state&63)/6]
	}
	return i - 1, uint8(state&63) / 2
}

func BenchmarkScan(b *testing.B) {
	dfa := MustCompile([]Rule{
		{Pattern: `[a-zA-Z0-9_]+`, Token: 1},
		{Pattern: `[^a-zA-Z0-9_]+`, Token: 2},
	}, Options{})

	keyw := "Benchmark123"
	str := strings.Repeat(keyw+strings.Repeat(" ", 30), 1000)
	var start int
	for start != len(str) {
		size, _ := dfa.Scan(str[start:])
		size2, _ := scanWord(str[start:])
		if size != size2 {
			b.Fatalf("dfa.Scan(%v) = %v, scanWord() = %v", str[start:], size, size2)
		}
		start += size
	}

	size, act := scanWord(str)
	if size != len(keyw) || act != 1 {
		b.Fatalf("Scan() = %v, %v; want: %v, %v", size, act, len(keyw), 1)
	}

	b.Run("DFA", func(b *testing.B) {
		b.SetBytes(int64(len(str)))
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			var start int
			for start != len(str) {
				size, _ := dfa.Scan(str[start:])
				start += size
			}
		}

	})
	b.Run("scanWordDFA", func(b *testing.B) {
		b.SetBytes(int64(len(str)))
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			var start int
			for start != len(str) {
				size, _ := scanWordDFA(str[start:])
				start += size
			}
		}

	})
	b.Run("scanWord", func(b *testing.B) {
		b.SetBytes(int64(len(str)))
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			var start int
			for start != len(str) {
				size, _ := scanWord(str[start:])
				start += size
			}
		}

	})

	utfText := strings.Repeat("AB \xdf\xb0 \xef\xbf\x82 	\xf0\x90\x80\x80", 4096)
	b.Run("isValidUTF8-baseline", func(b *testing.B) {
		b.SetBytes(int64(len(str)))
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			isValidUTF8(utfText)
		}

	})
	b.Run("utf8-Valid-baseline", func(b *testing.B) {
		b.SetBytes(int64(len(str)))
		b.ResetTimer()
		for i := 0; i < b.N; i++ {
			isValidUTF8(utfText)
		}
	})
}

var utf8Checker [256]uint64

func init() {
	emit := func(r1, r2, from, to int) {
		for r := r1; r < r2; r++ {
			utf8Checker[r] = utf8Checker[r]&^(63<<uint(from*6)) + uint64(to*6)<<uint(from*6)
		}
	}
	for i := 0; i <= 8; i++ {
		emit(0, 256, i, 8) // state 8 is the error state, stay there once reached
	}
	// ASCII
	emit(0, 0x80, 0, 0)
	// Two-byte sequences.
	emit(0xc2, 0xe0, 0, 1)
	emit(0x80, 0xc0, 1, 0)
	// Three-byte sequences.
	emit(0xe0, 0xe1, 0, 4) // next byte must be in [0xa0, 0xc0)
	emit(0xe1, 0xed, 0, 2)
	emit(0xed, 0xee, 0, 5) // next byte must be in [0x80, 0xa0)
	emit(0xee, 0xf0, 0, 2)
	emit(0x80, 0xc0, 2, 1)
	emit(0xA0, 0xc0, 4, 1)
	emit(0x80, 0xc0, 5, 1)
	// Four-byte sequences.
	emit(0xf0, 0xf1, 0, 6) // next byte must be in [0x90, 0xc0)
	emit(0xf1, 0xf4, 0, 3)
	emit(0xf4, 0xf5, 0, 7) // next byte must be in [0x80, 0x90)
	emit(0x80, 0xc0, 3, 2)
	emit(0x90, 0xc0, 6, 2)
	emit(0x80, 0x90, 7, 2)
}

// isValidUTF8 returns true if the input is a valid UTF-8 string.
//
// This function is implemented via a shift DFA and should theoretically
// run at 1 byte per CPU cycle.
func isValidUTF8(input string) bool {
	var state uint64
	var i int
	for ; i < len(input); i++ {
		row := utf8Checker[input[i]]
		state = row >> (state & 63)
	}
	return state&63 == 0
}

func TestIsValidUTF8(t *testing.T) {
	b := []byte{0, 0x80, 0x91, 0xa1, 0xc0, 0xe0, 0xf0, 0xf8, 0xf9}
	var str []byte
	for i := 0; i < 10000; i++ {
		str := str[:0]
		for e := i; e > 0; e /= len(b) {
			str = append(str, b[e%len(b)])
		}
		want := utf8.Valid(str)
		got := isValidUTF8(string(str))
		if got != want {
			t.Fatalf("isValidUTF8(%x) = %v, want: %v", str, got, want)
		}
	}
}
