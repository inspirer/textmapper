package gen

import (
	"embed"
	"fmt"

	"github.com/inspirer/textmapper/grammar"
)

var languages = map[string]*language{
	"go": {
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
	},

	"cc": {
		SharedDefs: builtin(`cc_shared`),
		Lexer: []file{
			{"token.h", builtin(`cc_token_h`)},
			{"lexer.h", builtin(`cc_lexer_h`)},
			{"lexer.cc", builtin(`cc_lexer_cc`)},
		},
		Parser: []file{
			{"parser.h", builtin(`cc_parser_h`)},
			{"parser.cc", builtin(`cc_parser_cc`)},
		},
	},
}

type file struct {
	name     string
	template string
}

type language struct {
	Lexer    []file
	Parser   []file
	Types    []file
	Selector []file
	AST      []file
	Bison    []file

	SharedDefs string
}

func (l *language) templates(g *grammar.Grammar) []file {
	var ret []file
	if g.Lexer.Tables != nil {
		ret = append(ret, l.Lexer...)
	} else {
		// Take the token file only.
		ret = append(ret, l.Lexer[0])
	}
	if g.Parser.Tables != nil {
		ret = append(ret, l.Parser...)
	}
	if g.Parser.Types != nil {
		ret = append(ret, l.Types...)
		if g.Options.GenSelector || g.Options.EventFields {
			ret = append(ret, l.Selector...)
		}
		if g.Options.EventFields && g.Options.EventAST {
			ret = append(ret, l.AST...)
		}
	}
	if g.Options.WriteBison {
		ret = append(ret, file{name: g.Name + ".y", template: bisonTpl})
	}
	return ret
}

var bisonTpl = builtin(`bison`)

//go:embed templates/*
var fs embed.FS

func builtin(name string) string {
	name = "templates/" + name + ".go.tmpl"
	content, err := fs.ReadFile(name)
	if err != nil {
		panic(fmt.Sprintf("cannot read %v: %v", name, err))
	}
	return string(content)
}
