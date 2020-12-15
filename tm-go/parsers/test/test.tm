# Testing field generation.

language test(go);

lang = "test"
package = "github.com/inspirer/textmapper/tm-go/parsers/test"
eventBased = true
eventFields = true
writeBison = true
debugParser = false
tokenLine = false
cancellable = true
reportTokens = [MultiLineComment, SingleLineComment, invalid_token, Identifier]
extraTypes = ["Int7", "Int9"]

:: lexer

WhiteSpace: /[ \t\r\n]/ (space)

SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/  (space)

Identifier: /[a-zA-Y](-*[a-zA-Z_0-9])*/    (class)

IntegerConstant {int}: /[0-9]+/ { $$ = mustParseInt(l.Text()) }

lastInt: /[0-9]+(\n|{eoi})/

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

dquote: /"/
squote: /'/

# No backtracking required.
hex = /[0-9a-fA-F]/
esc = /u{hex}{4}/
idChar = /[a-zA-Z]|\\{esc}/

SharpAtID: /Z{idChar}+/    (class)
invalid_token: /Z{idChar}*\\(u{hex}{0,3})?/
'Zfoo': /Zfoo/

# Backtracing required.
backtrackingToken: /test(foo)?-+>/

error:
invalid_token:
eoi:

%x inMultiLine;

# This is an example of how one can support nested block comments.
MultiLineComment:  /\/\*/ (space)
  {
    l.State = StateInMultiLine
    commentOffset = l.tokenOffset
    commentDepth = 0
    space = true
  }

<inMultiLine> {
  invalid_token: /{eoi}/
    {
      l.tokenOffset = commentOffset
      l.State = StateInitial
    }
  MultiLineComment: /\/\*/ (space)
    {
      commentDepth++
      space = true
    }
  MultiLineComment: /\*\// (space)
    {
      if commentDepth == 0 {
        space = false
        l.tokenOffset = commentOffset
        l.State = StateInitial
        break
      }
      space = true
      commentDepth--
    }

  # Note: space = true below is needed only during the migration period to help the Go and Java
  # implementations produce identical output (rather than just equivalent).
  WhiteSpace: /[^\/*]+|[*\/]/ (space) { space = true }
}

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
  | lastInt  -> LastInt
  | IntegerConstant ('[' ']')?
      {
        switch $IntegerConstant {
        case 7:
          p.listener(Int7, ${first().offset}, ${last().endoffset})
        case 9:
          p.listener(Int9, ${first().offset}, ${last().endoffset})
        }
      }                                                  -> Int
  | 'test' '{' set(~(eoi | '.' | '}'))* '}' -> TestClause
  | 'test' '(' (empty1 -> Empty1) ')'
  | 'test' (IntegerConstant -> Icon/InTest) -> TestIntClause/InTest,InFoo
;

empty1 : ;

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

${template go_lexer.onBeforeNext-}
	var commentOffset, commentDepth int
${end}