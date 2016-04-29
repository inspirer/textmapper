package json

import (
	"fmt"
)

type Symbol int

var symbolStr = [...]string{
	"JSONText",
	"JSONValue",
	"JSONValue_A",
	"JSONObject",
	"JSONMember",
	"JSONMemberList",
	"JSONArray",
	"JSONElementList",
	"JSONElementListopt",
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
	-1, -1, -3, 15, 16, 9, 10, 11, 12, 0, 13, 14, 18, -1, 20, -1,
	23, -23, -1, -1, 17, -1, -1, 22, 7, 8, 1, 2, 3, 4, 19, 5,
	6, 21, 24, -1, -2,
}

var tmLalr = []int32{
	1, -1, 3, -1, 8, -1, 9, -1, 11, -1, 12, -1, 13, -1, 14, -1,
	4, 26, -1, -2, 6, -1, 4, 25, -1, -2,
}

var tmGoto = []int32{
	0, 1, 5, 7, 11, 12, 13, 15, 15, 21, 25, 25, 29, 33, 37, 40,
	41, 41, 42, 43, 46, 50, 52, 53, 57, 58, 59,
}

var tmFrom = []int32{
	35, 0, 2, 19, 22, 1, 15, 0, 2, 19, 22, 18, 13, 15, 17, 0,
	1, 2, 19, 21, 22, 0, 2, 19, 22, 0, 2, 19, 22, 0, 2, 19,
	22, 0, 2, 19, 22, 0, 2, 22, 19, 0, 19, 0, 2, 22, 0, 2,
	19, 22, 1, 21, 1, 0, 2, 19, 22, 2, 2,
}

var tmTo = []int32{
	36, 1, 1, 1, 1, 12, 20, 2, 2, 2, 2, 23, 19, 21, 22, 3,
	13, 3, 24, 13, 3, 4, 4, 25, 4, 5, 5, 26, 5, 6, 6, 27,
	6, 7, 7, 28, 7, 8, 8, 8, 29, 35, 30, 9, 16, 34, 10, 10,
	31, 10, 14, 33, 15, 11, 11, 32, 11, 17, 18,
}

var tmRuleLen = []int32{
	1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	1, 3, 2, 3, 1, 3, 3, 1, 3, 1, 0,
}

var tmRuleSymbol = []int32{
	17, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19,
	19, 20, 20, 21, 22, 22, 23, 24, 24, 25, 25,
}

// set(first JSONValue<+A>)
var Literals = []int32{
	1,
}
