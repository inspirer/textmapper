#   Simple javascript grammar

.lang        "js" 
.prefix      "JsTest"
.positions   "line,offset"
.endpositions "offset"

# Vocabulary

Lid:        /[a-zA-Z_][a-zA-Z_0-9]*/	{ $lexem = token; }
_skip:      /\/\/.*/
_skip:      /[\t\r\n ]+/    { return false; }


# Grammar

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