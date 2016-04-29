package json

import (
	"fmt"
)

type Symbol int

var symbolStr = [...]string{
	"JSONText",
	"JSONValue",
	"JSONObject",
	"JSONMember",
	"JSONMemberList",
	"JSONArray",
	"JSONElementList",
}

func (n Symbol) String() string {
    if n < Symbol(terminalEnd) {
      return Token(n).String()
    }
    i := int(n) - int(terminalEnd)
	if i < len(symbolStr) {
		return symbolStr[i]
	}
	return fmt.Sprintf("nonterminal(%d)", n)
}

var tmAction = []int32{
	-1, -1, -1, 6, 7, 1, 2, 3, 0, 4, 5, 9, -1, 11, -1, 14,
	15, -1, -1, 8, -1, 13, -1, 10, 12, 16, -1, -2,
}

var tmGoto = []int32{
	0, 1, 5, 7, 11, 13, 14, 16, 16, 22, 26, 26, 30, 34, 38, 38,
	39, 43, 47, 49, 50, 54, 55,
}

var tmFrom = []int32{
	26, 0, 2, 18, 22, 1, 14, 0, 2, 18, 22, 2, 17, 12, 14, 17,
	0, 1, 2, 18, 20, 22, 0, 2, 18, 22, 0, 2, 18, 22, 0, 2,
	18, 22, 0, 2, 18, 22, 0, 0, 2, 18, 22, 0, 2, 18, 22, 1,
	20, 1, 0, 2, 18, 22, 2,
}

var tmTo = []int32{
	27, 1, 1, 1, 1, 11, 19, 2, 2, 2, 2, 15, 21, 18, 20, 22,
	3, 12, 3, 3, 12, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6,
	6, 6, 7, 7, 7, 7, 26, 8, 16, 23, 25, 9, 9, 9, 9, 13,
	24, 14, 10, 10, 10, 10, 17,
}

var tmRuleLen = []int32{
	1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 3, 1, 3, 3, 2, 1,
	3,
}

var tmRuleSymbol = []int32{
	15, 16, 16, 16, 16, 16, 16, 16, 17, 17, 18, 19, 19, 20, 20, 21,
	21,
}

// set(first JSONValue)
var Literals = []int32{
	1, 3, 8, 9, 11, 12, 13,
}
