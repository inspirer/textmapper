#   Simple javascript grammar

lang = "js"
prefix = "JsTest"
positions = "line,column,offset"
endpositions = "line,column,offset"

:: lexer

Lid:        /[a-zA-Z_][a-zA-Z_0-9]*/	{ $symbol = this.token; break; }
_skip:      /\/\/.*/
_skip:      /[\t\r\n ]+/    { return false; }

error:

:: parser

input ::= Lid ;

%%

${template js.parsercode-}
dump = alert;

function error(s) {
	dump(s);
}

function parse(string) {
	var p = new parser();
	p.buff = string;
	p.l = 0;
//	p.DEBUG_syntax = 1;
	p.parse();
}
${end}