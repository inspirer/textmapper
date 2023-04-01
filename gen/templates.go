package gen

import (
	"github.com/inspirer/textmapper/grammar"
)

type file struct {
	name     string
	template string
}

type language struct {
	Lexer  []file
	Parser []file
	Types  []file
	Bison  []file
}

func (l *language) templates(g *grammar.Grammar) []file {
	var ret []file
	ret = append(ret, l.Lexer...)
	if g.Parser.Tables != nil {
		ret = append(ret, l.Parser...)
	}
	if g.Parser.Types != nil {
		ret = append(ret, l.Types...)
	}
	if g.Options.WriteBison {
		ret = append(ret, file{name: g.Name + ".y", template: bisonTpl})
	}
	return ret
}

const bisonTpl = `%{
%}
{{range .Parser.Inputs}}
%start {{(index $.Parser.Nonterms .Nonterm).Name}}{{if .NoEoi}} // no-eoi{{end}}
{{- end}}
{{range .Parser.Prec}}
%{{.Associativity}}{{range .Terminals}} {{(index $.Syms .).ID}}{{end}}
{{- end}}
{{- range slice .TokensWithoutPrec 1}}
%token {{.ID}}
{{- end}}

%%
{{- range .Parser.Nonterms}}

{{ if eq .Value.Kind 11 -}}
// lookahead: {{ range $i, $it := .Value.Sub }}{{if gt $i 0}} & {{end}}{{$it}}{{end}}
{{ end -}}
{{.Name}} :
{{- if eq .Value.Kind 2 }}
{{- range $i, $rule := .Value.Sub}}
{{ if eq $i 0}}  {{else}}| {{end}}{{$.ExprString $rule}}
{{- end}}
{{- else if eq .Value.Kind 11 }}
  %empty
{{- else }}
  {{$.ExprString .Value}}
{{- end }}
;
{{- end}}

%%

`
