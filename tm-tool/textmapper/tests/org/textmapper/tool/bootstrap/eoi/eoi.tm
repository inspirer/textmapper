language eoi(java);

prefix = "Eoi"
package = "org.textmapper.tool.bootstrap.eoi"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"

:: lexer

%s a, b, c;

[initial, a, b, c]

id: /[a-zA-Z_]+/
':':        /:/
';':        /;/
',':        /,/
gotoc:      /<c>/   { state = States.c; }

_skip: /[\n\t\r ]+/  (space)

[initial]
'(':        /\(/    { state = States.a; }
')':        /\)/
_customEOI:       /{eoi}/  (space) 		{ if (--eoiToGo < 0) { $symbol = Tokens.eoi; spaceToken = false; } }

[a]
'(':        /\(/    { state = States.b; }
')':        /\)/    { state = States.initial; }
_retfromA:       /{eoi}/  (space)       { state = States.initial; }

[b]
'(':        /\(/
')':        /\)/  { state = States.a; }
_retfromB:       /{eoi}/  (space)       { state = States.a; }

[c]
eoi:  /{eoi}/

:: parser

input :
	  expr
;

expr :
	  id
	| '(' (id ':' expr separator ',')* ';' ')'?
;

%%

${template java_lexer.lexercode}
private int eoiToGo = 5;
${end}
