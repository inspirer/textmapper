# Testing field generation.

language test(go);

lang = "test"
package = "github.com/inspirer/textmapper/tm-go/parsers/test"
eventBased = true
eventFields = true
reportInvalidRunes = false
reportTokens = [MultiLineComment, SingleLineComment, invalid_token, Identifier]

:: lexer

WhiteSpace: /[ \t\r\n]/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment:  /\/\*{commentChars}\*\//    (space)
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/  (space)

Identifier: /[a-zA-Z_][a-zA-Z_0-9]*/    (class)

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

error:
invalid_token:

:: parser

%input Test;

QualifiedName ::=
	  Identifier
	| QualifiedName '.' Identifier
;

Test ::=
	  Declaration+ ;

Declaration interface ::=
	  Decl1
	| Decl2
	| '{' Declaration+ '}'        {~Block}
;

Decl1 ::=
	  'decl1' '(' QualifiedName ')' ;

Decl2 ::=
	  'decl2'
;
