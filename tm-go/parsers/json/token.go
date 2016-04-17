package json

import (
	"fmt"
)

type Token int

const (
	// An end-of-input marker token.
	EOI Token = iota

	LCURLY  // {
	RCURLY  // }
	LSQUARE // [
	RSQUARE // ]
	COLON   // :
	COMMA   // ,

	SPACE
	JSONSTR
	JSONNUM

	NULL
	TRUE
	FALSE

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
	"JSONSTR",
	"JSONNUM",

	"null",
	"true",
	"false",
}

func (tok Token) String() string {
	if tok >= 0 && int(tok) < len(tokenStr) {
		return tokenStr[tok]
	}
	return fmt.Sprintf("token(%d)", tok)
}
