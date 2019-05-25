# Testing field generation.

language test(go);

lang = "test"
package = "github.com/inspirer/textmapper/tm-go/parsers/test"
eventBased = true
eventFields = true
tokenLine = false
cancellable = true
reportTokens = [MultiLineComment, SingleLineComment, invalid_token, Identifier]
extraTypes = ["Int7", "Int9"]

:: lexer

WhiteSpace: /[ \t\r\n]/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment:  /\/\*{commentChars}\*\//    (space)
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/  (space)

Identifier: /[a-zA-Z_](-*[a-zA-Z_0-9])*/    (class)

IntegerConstant {int}: /[0-9]+/ { $$ = mustParseInt(l.Text()) }

# Keywords.
'test':      /test/
'decl1':      /decl1/
'decl2':      /decl2/

# Punctuation
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'.': /\./
',': /,/
':': /:/
'-': /-/
'->': /->/

# Backtracing.
backtrackingToken: /test(foo)?-+>/

error:
invalid_token:

:: parser

%input Test, Decl1;

Test -> Test :
    Declaration+ ;

# Test: an interface with a type rule.

%interface Declaration;

Declaration -> Declaration :
    Decl1
  | Decl2
  | '{' ('-' '-'? -> Negation)? Declaration+? '}'        -> Block
  | IntegerConstant ('[' ']')?
      {
        switch $IntegerConstant {
        case 7:
          p.listener(Int7, ${first().offset}, ${last().endoffset})
        case 9:
          p.listener(Int9, ${first().offset}, ${last().endoffset})
        }
      }                                                  -> Int
;

# Test: a list of an exported terminal.

QualifiedName :
    Identifier
  | QualifiedName '.' Identifier
;

Decl1 {int} -> Decl1 :
    'decl1' '(' QualifiedName ')' ;

Decl2 :
    'decl2' -> Decl2 ;

%%

${template go_lexer.lexer-}
${call base-}

func mustParseInt(s string) int {
	i, err := "strconv".Atoi(s)
	if err != nil {
		panic(`lexer internal error: ` + err.Error())
	}
	return i
}
${end}

${template newTemplates-}
{{define "onAfterLexer"}}
func mustParseInt(s string) int {
	i, err := "strconv".Atoi(s)
	if err != nil {
		panic(`lexer internal error: ` + err.Error())
	}
	return i
}
{{end}}
${end}
