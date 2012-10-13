#  syntax: sample2 grammar

#  Copyright 2002-2012 Evgeny Gryaznov
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
package = "org.textmapper.tool.test.bootstrap.b"
maxtoken = 2048
breaks = true
genast = true
gentree = true
positions = "offset"
endpositions = "offset"
genCleanup = false
genCopyright = true


identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ (class)
												{ $lexem = current(); }
_skip:          /[\n\t\r ]+/ (space)

Lclass: /class/								{ $lexem = "class"; }
Lextends: /extends/  (soft)
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/

# reserved

Linterface: /interface/                      { $lexem = "interface"; }
Lenum(Object):      /enum/							 { $lexem = new Object(); }


error:

# class without instances
numeric: /0x[0-9a-fA-F]+/	(class)

# class without instances; action
octal:  /0[0-7]+/			(class)   { $lexem = Integer.parseInt(current(), 8); }

# class with instance
decimal:  /[1-9][0-9]+/			(class)

# instance of decimal
eleven:   /11/				          { $lexem = 11; }

# soft
_skipSoftKW: /xyzzz/	(soft)


# grammar

%input classdef_NoEoi no-eoi;

classdef_NoEoi ::=
	classdef ;

classdef ::=
	tc=Lclass ID '{' classdeflistopt '}'
  | tc=Lclass ID te=Lextends identifier '{' classdeflistopt '}'
;

ID ::=
	@pass identifier ;

classdeflist ::=
	classdef
  | classdeflist classdef
  | identifier '(' ')'
  | identifier '(' Lextends ')'				{ String s = /* should be string */ $Lextends; }
  | classdeflist identifier '(' ')'
  | error
;
