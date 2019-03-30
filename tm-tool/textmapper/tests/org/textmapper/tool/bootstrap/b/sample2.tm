#  syntax: sample2 grammar

#  Copyright 2002-2018 Evgeny Gryaznov
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

language sample2(java);

prefix = "SampleB"
package = "org.textmapper.tool.bootstrap.b"
maxtoken = 2048
breaks = true
genast = true
gentree = true
genmain = true
defaultExtension = "b"
positions = "offset"
endpositions = "offset"
genCleanup = false
genCopyright = true

:: lexer

identifier {String}: /[a-zA-Z_][a-zA-Z_0-9]*/ (class)
												{ $$ = tokenText(); }
_skip:          /[\n\t\r ]+/ (space)

Lclass: /class/								{ $$ = "class"; }
Lextends: /extends/
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/

# reserved

Linterface: /interface/                      { $$ = "interface"; }
Lenum {Object}:      /enum/							 { $$ = new Object(); }


error:

# class without instances
numeric: /0x[0-9a-fA-F]+/	(class)

# class without instances; action
octal:  /0[0-7]+/			(class)   { $$ = Integer.parseInt(tokenText(), 8); }

# class with instance
decimal:  /[1-9][0-9]+/			(class)

# instance of decimal
eleven:   /11/				          { $$ = 11; }


:: parser

%input classdef_no_eoi no-eoi;

classdef_no_eoi interface :
	classdef ;

classdef :
	tc=Lclass ID '{' classdeflistopt '}'
  | tc=Lclass ID Lextends identifier '{' classdeflistopt '}'
;

ID :
	@pass identifier ;

classdeflist :
	classdef
  | classdeflist classdef
  | identifier '(' ')'
  | classdeflist identifier '(' ')'
  | error
;
