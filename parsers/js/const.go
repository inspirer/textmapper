package js

import (
	"github.com/inspirer/textmapper/parsers/js/token"
)

const (
	keywordStart = token.PRIVATEIDENTIFIER + 1
	keywordEnd   = token.LBRACE

	punctuationStart = token.LBRACE
	punctuationEnd   = token.NUMERICLITERAL
)
