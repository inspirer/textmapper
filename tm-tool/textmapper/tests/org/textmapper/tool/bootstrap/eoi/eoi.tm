#  Copyright 2002-2019 Evgeny Gryaznov
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

language eoi(java);

prefix = "Eoi"
package = "org.textmapper.tool.bootstrap.eoi"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"

:: lexer

%s initial, a, b, c;

id: /[a-zA-Z_]+/
':':        /:/
';':        /;/
',':        /,/
gotoc:      /<c>/   { state = States.c; }

_skip: /[\n\t\r ]+/  (space)

<initial> {
  '(':        /\(/    { state = States.a; }
  ')':        /\)/
  _customEOI:       /{eoi}/  (space) 		{ if (--eoiToGo < 0) { $symbol = Tokens.eoi; spaceToken = false; } }
}

<a> {
  '(':        /\(/    { state = States.b; }
  ')':        /\)/    { state = States.initial; }
  _retfromA:       /{eoi}/  (space)       { state = States.initial; }
}

<b> {
  '(':        /\(/
  ')':        /\)/  { state = States.a; }
  _retfromB:       /{eoi}/  (space)       { state = States.a; }
}

<c>
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
