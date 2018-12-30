package grammar

import (
	"testing"
)

var idTests = []struct {
	input string
	style nameStyle
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
	{"abc-def", CamelCase, "AbcDef"},
	{"abc_def38", CamelCase, "AbcDef38"},

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
	{"'0'", UpperCase, "CHAR_X30"},
	{"'00'", UpperCase, "X30X30"},
	{"'0'", UpperUnderscores, "CHAR_X30"},
	{"'00'", UpperUnderscores, "X30_X30"},
}

func TestSymbolID(t *testing.T) {
	for _, tc := range idTests {
		if got := SymbolID(tc.input, tc.style); got != tc.want {
			t.Errorf("SymbolID(%v,%v) = %v, want: %v", tc.input, tc.style, got, tc.want)
		}
	}
}
