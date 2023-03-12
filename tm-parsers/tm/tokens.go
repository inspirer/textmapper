package tm

import (
	"github.com/inspirer/textmapper/tm-parsers/tm/token"
)

func IsSoftKeyword(t token.Token) bool {
	return t >= token.ASSERT && t <= token.CHAR_X
}

func IsKeyword(t token.Token) bool {
	return t >= token.AS && t <= token.TRUE
}
