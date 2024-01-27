package shiftdfa

import (
	"fmt"
	"strings"
	"testing"
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
	if c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' {
		for i := 1; i < len(input); i++ {
			c := input[i]
			if !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
				return i, 1
			}
		}
		return len(input), 1
	}

	for i := 1; i < len(input); i++ {
		c := input[i]
		if c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' {
			return i, 1
		}
	}
	return len(input), 1
}

func BenchmarkSearch(b *testing.B) {
	dfa := MustCompile([]Rule{
		{Pattern: `[a-zA-Z]+`, Token: 1},
		{Pattern: `[^a-zA-Z]+`, Token: 2},
	}, Options{})

	keyw := "Benchmark"
	str := strings.Repeat(keyw+strings.Repeat(" ", 30), 10)
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
}
