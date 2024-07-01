package ident

import (
	"testing"
)

var idTests = []struct {
	input string
	style Style
	want  string
}{
	{"abc", CamelCase, "Abc"},
	{"abcDef", CamelCase, "AbcDef"},
	{"FooID", CamelCase, "FooId"},
	{"FooIDBar", CamelCase, "FooIdBar"},
	{"fooIDBar", CamelCase, "FooIdBar"},
	{"fooIDBar", CamelLower, "fooIdBar"},
	{"FooIDBar", UpperUnderscores, "FOO_ID_BAR"},
	{"fooIDBar", UpperUnderscores, "FOO_ID_BAR"},
	{"AbcDEF", UpperUnderscores, "ABC_DEF"},
	{"ABcDEF", UpperUnderscores, "A_BC_DEF"},
	{"ABcDEF", CamelLower, "aBcDef"},
	{"ABCDEF", UpperUnderscores, "ABCDEF"},
	{"ABCDEF", UpperCase, "ABCDEF"},
	{"abc_def", UpperCase, "ABC_DEF"},
	{"abc_def", UpperUnderscores, "ABC_DEF"},
	{"abc-def", CamelCase, "AbcDef"},
	{"abc_def38", CamelCase, "AbcDef38"},

	{"a-1", CamelCase, "A1"},
	{"a-1", CamelLower, "a1"},
	{"a-1", UpperCase, "A1"},
	{"a-1", UpperUnderscores, "A_1"},
	{"a_1", CamelCase, "A1"},

	{"A$1", CamelCase, "A_1"},
	{"abc_def$12", CamelCase, "AbcDef_12"},
	{"abc-def$12", CamelCase, "AbcDef_12"},
	{"ABCDef$2", CamelCase, "AbcDef_2"},

	{"'}'", UpperCase, "RBRACE"},
	{"'}'", CamelCase, "Rbrace"},
	{"'}'", CamelLower, "rbrace"},
	{"'}'", UpperUnderscores, "RBRACE"},
	{"'++'", UpperCase, "PLUSPLUS"},
	{"'++'", CamelCase, "PlusPlus"},
	{"'++'", CamelLower, "plusPlus"},
	{"'++'", UpperUnderscores, "PLUS_PLUS"},

	{"'a\u789a'", UpperCase, "AU00789A"},
	{"'a\u789a'", UpperUnderscores, "A_U00789A"},
	{"'a\u789a'", CamelCase, "AU00789a"},
	{"'a\u789a'", CamelLower, "aU00789a"},
	{"'\x7f'", UpperCase, "CHAR_X7F"},
	{"'\x7f'", UpperUnderscores, "CHAR_X7F"},
	{"'\x7f'", CamelCase, "CharX7f"},
	{"'\x7f'", CamelLower, "charX7f"},

	{"'keyword'", CamelLower, "keyword"},
	{"'keyword'", CamelCase, "Keyword"},
	{"'keyword'", UpperCase, "KEYWORD"},
	{"'keyword'", UpperUnderscores, "KEYWORD"},
	{"'keywordFoo'", CamelLower, "keywordFoo"},
	{"'keywordFoo'", CamelCase, "KeywordFoo"},
	{"'keywordFoo'", UpperCase, "KEYWORDFOO"},
	{"'keywordFoo'", UpperUnderscores, "KEYWORD_FOO"},

	{"'keyword foo'", CamelLower, "keywordSpaceFoo"},

	{"'s'", CamelCase, "CharS"},
	{"'s'", CamelLower, "charS"},
	{"'s'", UpperCase, "CHAR_S"},
	{"'s'", UpperUnderscores, "CHAR_S"},
	{"'s0'", UpperCase, "S0"},
	{"'s0'", UpperUnderscores, "S0"},
	{"'0'", UpperCase, "CHAR_0"},
	{"'00'", UpperCase, "_00"},
	{"'0'", UpperUnderscores, "CHAR_0"},
	{"'00'", UpperUnderscores, "_00"},

	{"'_'", UpperCase, "CHAR__"},
	{"'__'", UpperCase, "__"},
	{"'f_a'", UpperCase, "F_A"},
	{"'foo_'", UpperCase, "FOO_"},
	{"'_'", UpperUnderscores, "CHAR__"},
	{"'__'", UpperUnderscores, "__"},
	{"'f_a'", UpperUnderscores, "F_A"},
	{"'foo_'", UpperUnderscores, "FOO_"},

	{`"__"`, UpperUnderscores, "__"},
	{`"f_a"`, UpperUnderscores, "F_A"},

	// Special cases (compatibility with legacy Textmapper).
	{`'\'`, UpperCase, "ESC"},
	{`'\''`, UpperCase, "APOS"},
	{`'\''`, CamelLower, "apos"},
	{`'\\'`, UpperCase, "ESC"},
}

func TestSymbolID(t *testing.T) {
	for _, tc := range idTests {
		got := Produce(tc.input, tc.style)
		if got != tc.want {
			t.Errorf("Produce(%v,%v) = %v, want: %v", tc.input, tc.style, got, tc.want)
		}
		if !IsValid(got) {
			t.Errorf("Produce(%v,%v) = %v (invalid identifier)", tc.input, tc.style, got)
		}
	}
}

var isValidTests = []struct {
	input string
	want  bool
}{
	{"", false},
	{"1", false},
	{"a", true},
	{"abc123", true},
	{"ABC", true},
	{"alpha", true},
	{"alpha ", false},
	{"\nalpha", false},
}

func TestIsValid(t *testing.T) {
	for _, tc := range isValidTests {
		if got := IsValid(tc.input); got != tc.want {
			t.Errorf("IsValid(%q) = %v, want: %v", tc.input, got, tc.want)
		}
	}
}
