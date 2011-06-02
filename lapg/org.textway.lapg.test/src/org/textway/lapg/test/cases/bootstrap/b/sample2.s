#  syntax: sample2 grammar

#  Copyright 2002-2011 Evgeny Gryaznov
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

lang = "java"
prefix = "SampleB"
package = "org.textway.lapg.test.cases.bootstrap.b"
maxtoken = 2048
breaks = true
genast = true
gentree = true
positions = "offset"
endpositions = "offset"
genCleanup = false

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ (class)
												{ $lexem = current(); break; }
_skip:          /[\n\t\r ]+/                   	{ return false; }

Lclass: /class/								{ $lexem = "class"; break; }
Lextends: /extends/  (soft)
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/

# reserved

Linterface: /interface/                      { $lexem = "interface"; break; }
Lenum(Object):      /enum/							 { $lexem = new Object(); break; }


error:

# class without instances
numeric: /0x[0-9a-fA-F]+/	(class)

# class without instances; action
octal:  /0[0-7]+/			(class)   { $lexem = Integer.parseInt(current(), 8); break; }

# class with instance
decimal:  /[1-9][0-9]+/			(class)

# instance of decimal
eleven:   /11/				          { $lexem = 11; break; }

# soft
_skipSoftKW: /xyzzz/	(soft)


# grammar

%input classdef_NoEoi ;

classdef_NoEoi ::=
	classdef ;

classdef ::=
	Lclass ID '{' classdeflistopt '}'
  | Lclass ID Lextends identifier '{' classdeflistopt '}'
;

ID ::=
	@pass identifier ;

classdeflist ::=
	classdef
  | classdeflist classdef
  | identifier '(' ')'
  | classdeflist identifier '(' ')'
  | error
;
