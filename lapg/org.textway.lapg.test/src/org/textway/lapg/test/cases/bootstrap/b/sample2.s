# lapg syntax file

lang = "java"
prefix = "SampleA"
package = "org.textway.lapg.test.cases.bootstrap.b"
maxtoken = 2048
breaks = true
genast = true
gentree = true
positions = "offset"
endpositions = "offset"
genCleanup = false

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ -1 (class)
												{ $lexem = current(); break; }
_skip:          /[\n\t\r ]+/                   	{ return false; }

Lclass: /class/								{ $lexem = "class"; break; }
'{': /\{/
'}': /\}/

# reserved

Linterface: /interface/                      { $lexem = "interface"; break; }
Lenum(Object):      /enum/							 { $lexem = new Object(); break; }


error:

# grammar


%input classdef_NoEoi ;

classdef_NoEoi ::=
	classdef ;

classdef ::=
	Lclass identifier '{' classdeflistopt '}' ;

classdeflist ::=
	classdef
  | classdeflist classdef
  | error
;
