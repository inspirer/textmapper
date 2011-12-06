#  syntax: sample1 grammar

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
prefix = "SampleA"
package = "org.textway.lapg.test.cases.bootstrap.a"
maxtoken = 2048
breaks = true
genast = true
gentree = true
positions = "line,column,offset"
endpositions = "line,column,offset"
genCleanup = false
genCopyright = true

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ -1 { $lexem = current(); break; }
_skip:          /[\n\t\r ]+/                   	{ return false; }

Lclass: /class/
'{': /\{/
'}': /\}/

error:

# grammar


%input classdef_NoEoi no-eoi, classdef ;

classdef_NoEoi ::=
	classdef ;

classdef ::=
	Lclass identifier '{' classdeflistopt '}' ;

classdeflist ::=
	classdef
  | classdeflist classdef
  | error
;
