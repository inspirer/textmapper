package gen

import _ "embed"

var golang = &language{
	Lexer: []file{
		{"token/token.go", tokenTpl},
		{"lexer_tables.go", lexerTablesTpl},
		{"lexer.go", lexerTpl},
	},
	Parser: []file{
		{"parser.go", parserTpl},
		{"parser_tables.go", parserTablesTpl},
	},
	Types: []file{
		{"listener.go", parserListenerTpl},
	},
	Selector: []file{
		{"selector/selector.go", selectorTpl},
	},
}

//go:embed templates/go_shared.go.tmpl
var sharedDefs string

//go:embed templates/go_token.go.tmpl
var tokenTpl string

//go:embed templates/go_lexer_tables.go.tmpl
var lexerTablesTpl string

//go:embed templates/go_lexer.go.tmpl
var lexerTpl string

//go:embed templates/go_parser.go.tmpl
var parserTpl string

//go:embed templates/go_parser_tables.go.tmpl
var parserTablesTpl string

//go:embed templates/go_listener.go.tmpl
var parserListenerTpl string

//go:embed templates/go_selector.go.tmpl
var selectorTpl string
