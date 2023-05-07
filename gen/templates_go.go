package gen

import _ "embed"

var golang = &language{
	SharedDefs: builtin(`go_shared`),
	Lexer: []file{
		{"token/token.go", builtin(`go_token`)},
		{"lexer_tables.go", builtin(`go_lexer_tables`)},
		{"lexer.go", builtin(`go_lexer`)},
	},
	Parser: []file{
		{"parser.go", builtin(`go_parser`)},
		{"parser_tables.go", builtin(`go_parser_tables`)},
	},
	Types: []file{
		{"listener.go", builtin(`go_listener`)},
	},
	Selector: []file{
		{"selector/selector.go", builtin(`go_selector`)},
	},
	AST: []file{
		{"ast/tree.go", builtin(`go_tree`)},
		{"ast/parse.go", builtin(`go_ast_parse`)},
	},
}
