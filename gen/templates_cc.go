package gen

import _ "embed"

var cc = &language{
	SharedDefs: builtin(`cc_shared`),
	Lexer: []file{
		{"lexer.h", builtin(`cc_lexer_h`)},
	},
}
