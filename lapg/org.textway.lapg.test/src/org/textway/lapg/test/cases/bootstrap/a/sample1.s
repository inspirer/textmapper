# lapg syntax file

lang = "java"
prefix = "SampleA"
package = "org.textway.lapg.test.cases.bootstrap.a"
maxtoken = 2048
breaks = true
genast = true
gentree = true
positions = "line,column,offset"
endpositions = "line,column,offset"
genCleanup = false

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ -1 { $lexem = current(); break; }
_skip:          /[\n\t\r ]+/                   	{ return false; }

Lclass: /class/
'{': /\{/
'}': /\}/

error:

# grammar


%input classdef_NoEoi classdef ;

classdef_NoEoi ::=
	classdef ;

classdef ::=
	Lclass identifier '{' classdeflistopt '}' ;

classdeflist ::=
	classdef
  | classdeflist classdef
  | error
;
