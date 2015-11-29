language eoi(java);

prefix = "Eoi"
package = "org.textmapper.tool.bootstrap.eoi"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"

:: lexer

[initial, a, b, c]

id: /[a-zA-Z_]+/
':':        /:/
';':        /;/
',':        /,/
gotoc:      /<c>/ => c

_skip: /[\n\t\r ]+/  (space)

[initial]
'(':        /\(/  => a
')':        /\)/
_customEOI:       /{eoi}/  (space) 		{ if (--eoiToGo < 0) { $symbol = Tokens.eoi; spaceToken = false; } }

[a]
'(':        /\(/  => b
')':        /\)/  => initial
_retfromA:       /{eoi}/  => initial (space)

[b]
'(':        /\(/
')':        /\)/  => a
_retfromB:       /{eoi}/  => a (space)

[c]
eoi:  /{eoi}/

:: parser

input ::=
	  expr
;

expr ::=
	  id
	| '(' (id ':' expr separator ',')* ';' ')'?
;

%%

${template java_lexer.lexercode}
private int eoiToGo = 5;
${end}
