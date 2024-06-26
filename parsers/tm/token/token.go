// generated by Textmapper; DO NOT EDIT

package token

import (
	"fmt"
)

// Type is an enum of all terminal symbols of the tm language.
type Type int32

// Token values.
const (
	UNAVAILABLE Type = iota - 1
	EOI
	INVALID_TOKEN
	SCON
	ICON
	TEMPLATES // %%
	WHITESPACE
	COMMENT
	MULTILINECOMMENT
	REM               // %
	COLONCOLON        // ::
	OR                // |
	OROR              // ||
	ASSIGN            // =
	ASSIGNASSIGN      // ==
	EXCLASSIGN        // !=
	SEMICOLON         // ;
	DOT               // .
	COMMA             // ,
	COLON             // :
	LBRACK            // [
	RBRACK            // ]
	LPAREN            // (
	LPARENQUESTASSIGN // (?=
	MINUSGT           // ->
	RPAREN            // )
	RBRACE            // }
	LT                // <
	GT                // >
	MULT              // *
	PLUS              // +
	PLUSASSIGN        // +=
	QUEST             // ?
	EXCL              // !
	TILDE             // ~
	AND               // &
	ANDAND            // &&
	DOLLAR            // $
	AT                // @
	DIV               // /
	LBRACE            // {
	ERROR
	ID
	QUOTED_ID
	AS            // as
	FALSE         // false
	IMPORT        // import
	SEPARATOR     // separator
	SET           // set
	TRUE          // true
	ASSERT        // assert
	BRACKETS      // brackets
	CLASS         // class
	EMPTY         // empty
	EXPECT        // expect
	EXPECTMINUSRR // expect-rr
	EXPLICIT      // explicit
	EXTEND        // extend
	FLAG          // flag
	GENERATE      // generate
	GLOBAL        // global
	INJECT        // inject
	INLINE        // inline
	INPUT         // input
	INTERFACE     // interface
	LALR          // lalr
	LANGUAGE      // language
	LAYOUT        // layout
	LEFT          // left
	LEXER         // lexer
	LOOKAHEAD     // lookahead
	NOMINUSEOI    // no-eoi
	NONASSOC      // nonassoc
	NONEMPTY      // nonempty
	PARAM         // param
	PARSER        // parser
	PREC          // prec
	RIGHT         // right
	CHAR_S        // s
	SHIFT         // shift
	SPACE         // space
	CHAR_X        // x
	CODE          // {
	REGEXP

	NumTokens
)

var tokenStr = [...]string{
	"EOI",
	"INVALID_TOKEN",
	"SCON",
	"ICON",
	"%%",
	"WHITESPACE",
	"COMMENT",
	"MULTILINECOMMENT",
	"%",
	"::",
	"|",
	"||",
	"=",
	"==",
	"!=",
	";",
	".",
	",",
	":",
	"[",
	"]",
	"(",
	"(?=",
	"->",
	")",
	"}",
	"<",
	">",
	"*",
	"+",
	"+=",
	"?",
	"!",
	"~",
	"&",
	"&&",
	"$",
	"@",
	"/",
	"{",
	"ERROR",
	"ID",
	"QUOTED_ID",
	"as",
	"false",
	"import",
	"separator",
	"set",
	"true",
	"assert",
	"brackets",
	"class",
	"empty",
	"expect",
	"expect-rr",
	"explicit",
	"extend",
	"flag",
	"generate",
	"global",
	"inject",
	"inline",
	"input",
	"interface",
	"lalr",
	"language",
	"layout",
	"left",
	"lexer",
	"lookahead",
	"no-eoi",
	"nonassoc",
	"nonempty",
	"param",
	"parser",
	"prec",
	"right",
	"s",
	"shift",
	"space",
	"x",
	"{",
	"REGEXP",
}

func (tok Type) String() string {
	if tok >= 0 && int(tok) < len(tokenStr) {
		return tokenStr[tok]
	}
	return fmt.Sprintf("token(%d)", tok)
}
