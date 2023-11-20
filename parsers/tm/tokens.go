package tm

import (
	"github.com/inspirer/textmapper/parsers/tm/token"
)

func IsSoftKeyword(t token.Type) bool {
	return t >= token.ASSERT && t <= token.CHAR_X
}

func IsKeyword(t token.Type) bool {
	return t >= token.AS && t <= token.TRUE
}
