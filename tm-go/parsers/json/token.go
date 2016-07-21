package json

import (
	"fmt"
)

type Token int

const (
	UNAVAILABLE Token = iota - 1

	// An end-of-input marker token.
	EOI

	LBRACE // {
	RBRACE // }
	LBRACK // [
	RBRACK // ]
	COLON // :
	COMMA // ,
	SPACE
	MULTILINECOMMENT
	JSONSTRING
	JSONNUMBER
	ID
	NULL // null
	TRUE // true
	FALSE // false
	CHAR_A // A
	CHAR_B // B
	ERROR

	terminalEnd
)

var tokenStr = [...]string{
	"EOF",

	"{",
	"}",
	"[",
	"]",
	":",
	",",
	"SPACE",
	"MULTILINECOMMENT",
	"JSONSTRING",
	"JSONNUMBER",
	"ID",
	"null",
	"true",
	"false",
	"A",
	"B",
	"ERROR",
}

func (tok Token) String() string {
	if tok >= 0 && int(tok) < len(tokenStr) {
		return tokenStr[tok]
	}
	return fmt.Sprintf("token(%d)", tok)
}
