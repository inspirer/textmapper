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
	JSONSTRING
	JSONNUMBER
	NULL // null
	TRUE // true
	FALSE // false
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
	"JSONSTRING",
	"JSONNUMBER",
	"null",
	"true",
	"false",
	"ERROR",
}

func (tok Token) String() string {
	if tok >= 0 && int(tok) < len(tokenStr) {
		return tokenStr[tok]
	}
	return fmt.Sprintf("token(%d)", tok)
}
