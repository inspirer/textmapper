# Simple javascript grammar

language lexer1(js);

module = "lexer1"
positions = "line,column,offset"
endpositions = "line,column,offset"

:: lexer

id:        /[a-zA-Z_][a-zA-Z_0-9]*/  (class)	{ $symbol = this.token; }
icon:		/[0-9]+/
_skip:      /\/\/.*/       (space)
_skip:      /[\t\r\n ]+/    (space)

run:  /run/
class: /class/ (soft)

[state2]
method:  /method/
