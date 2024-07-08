# Testing field generation.

language test(go);

lang = "test"
package = "github.com/inspirer/textmapper/parsers/test"
eventBased = true
eventFields = true
writeBison = true
debugParser = false
tokenLine = false
fixWhitespace = true
cancellable = true
cancellableFetch = true
recursiveLookaheads = true
extraTypes = ["Int7", "Int9 -> Expr"]

:: lexer

WhiteSpace: /[ \t\r\n\x00]/ (space)

SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/  (space)

Identifier: /[a-zA-Y](-*[a-zA-Z_0-9])*/    (class)
Identifier2: /^[\p{Any}-[\x80-\U0010ffff]-\p{Lu}]/

IntegerConstant {int}: /[0-9]+/ { $$ = mustParseInt(l.Text()) }

lastInt: /[0-9]+(\n|{eoi})/

# Keywords.
'test':      /test/
'decl1':     /decl1/
'decl2':     /decl2/
'eval':      /eval/
'as':        /as/

'if': /if/
"else": /else/

# Punctuation
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'.': /\./
'...': /\.\.\./
',': /,/
':': /:/
'-': /-/
'->': /->/
'+': /\+/
'\\': /\\/
'_': /_/
'foo_': /foo_/
'f_a': /f_a/

multiline: /%\s*q((\n|{eoi})%\s*q)+/

dquote: /"/
'\'' (squote): /'/

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

%inject SingleLineComment -> SingleLineComment;
%inject Identifier -> Identifier;
%inject invalid_token -> InvalidToken;
%inject MultiLineComment -> MultiLineComment;

Test -> Test:
    Declaration+ ;

# Test: an interface with a type rule.

%interface Declaration;

Declaration -> Declaration :
    Decl1
  | Decl2
  | '{' ('-' '-'? -> Negation)? Declaration+? '}'        -> Block
  | lastInt  { println("it works") } -> LastInt
  | IntegerConstant ('[' ']')?
      {
        switch $IntegerConstant {
        case 7:
          p.listener(Int7, 0, ${self[0].offset}, ${last().endoffset})
        case 9:
          p.listener(Int9, 0, ${first().offset}, ${last().endoffset})
        }
      }                                                  -> Int
  | 'test' '{' set(~(eoi | '.' | '}'))* '}' -> TestClause
  | 'test' '(' (empty1 -> Empty1) ')'
  | 'test' '(' foo_nonterm<~A> ')'
  | 'test' (IntegerConstant -> Icon/InTest) -> TestIntClause/InTest,InFoo
  | 'eval' (?= !FooLookahead) '(' expr ')' empty1   -> EvalEmpty1
  | 'eval' (?= FooLookahead) '(' foo_nonterm<+A> ')' -> EvalFoo
  | 'eval' (?= FooLookahead) '(' IntegerConstant '.' a=expr '+' .greedy b=expr ')' -> EvalFoo2
  | 'decl2' ':' QualifiedNameopt -> DeclOptQual
;

FooLookahead:
  '(' IntegerConstant '.' set(foo_la)+ ')' ;

empty1 : ;

%flag A;

foo_la:
      IntegerConstant '.' expr
    | IntegerConstant 'foo_' expr
;

foo_nonterm<A> :
      IntegerConstant '.' expr 
    | [A] IntegerConstant 'foo_' expr
;

# Test: a list of an exported terminal.

QualifiedName :
    Identifier
  | QualifiedName '.' Identifier
;

Decl1 {int} -> Decl1 :
    'decl1' '(' QualifiedName ')' ;

%interface Decl2Interface;

Decl2 -> Decl2Interface :
    'decl2' -> Decl2
  | If
;

%expect 1;

If -> If:
    'if' '(' O ')' then=Decl2[then]   { /* 4: $then */ }
  | 'if' '(' O ')' then=Decl2 "else" else=Decl2
;

%nonassoc 'as';
%left '+';

%interface Expr;

expr -> Expr:
    left=expr '+' right=primaryExpr   -> PlusExpr
  | customPlus -> __ignoreContent
  | primaryExpr
;

O :
      elem+ ;

elem -> Elem:
      'f_a' .greedy | 'f_a' 'as' | 'as' ;  # prefer reduce over shift


customPlus -> __ignoreContent:
  '\\' primaryExpr '+' expr { p.listener(PlusExpr, 0, ${first().offset}, ${last().endoffset}) } ;

# AsExpr consumes the whole suffix greedily.
# Note: applying .greedy after 'as' does not work since the conflict happens later.
primaryExpr<flag WithoutAs = false> -> Expr:
    [!WithoutAs] left=primaryExpr<+WithoutAs> 'as' right=expr  -> AsExpr
;

extend primaryExpr -> Expr:
  (/*empty*/ -> Bar) IntegerConstant -> IntExpr ;

%%

{{define "onAfterLexer"}}
func mustParseInt(s string) int {
	i, err := "strconv".Atoi(s)
	if err != nil {
		panic(`lexer internal error: ` + err.Error())
	}
	return i
}
{{end}}

{{define "onBeforeNext"}}
	var commentOffset, commentDepth int
{{end}}

{{define "onAfterParser"}}
func parserEnd() {}
{{end}}

{{define "customReportNext"}}
default:
   break
{{end}}
