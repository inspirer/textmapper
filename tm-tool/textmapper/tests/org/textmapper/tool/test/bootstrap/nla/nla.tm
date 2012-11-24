#  syntax: nla test grammar

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
prefix = "NlaTest"
package = "org.textmapper.tool.test.bootstrap.nla"
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCopyright = true

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ (class)   { $symbol = current(); }
icon(Integer):  /-?[0-9]+/                     { $symbol = Integer.parseInt(current()); }
_skip:          /[\n\t\r ]+/             (space)

'=': /=/
';': /;/
'+': /+/
'-': /-/
'*': /*/
'/': /\//
'(': /\(/
')': /\)/
'{': /\{/
'}': /\}/
'[': /\[/
']': /\]/
',': /,/
'.': /\./
'!': /!/
'?': /?/
':': /:/
'->': /->/

Lnull: /null/
Linvoke: /invoke/  (soft)
Lreset: /reset/
Lnop: /nop/
Lexotic: /exotic/

# grammar

input ::=
	statements ;

statements ::=
	  statements statement
	| statement
;

statement ::=
	  control_statement
	| expression ';'
	| Lnop ';'
	| Lexotic exotic_call ';'
;

control_statement ::=
	reset_statement ;

reset_statement ::=
	Lreset identifier ';' ;

primary_expression ::=
  	  identifier
    | '(' expression ')'
	| icon
  	| Lnull
    | identifier '(' expression_listopt ')'
    | primary_expression '.' identifier
    | primary_expression '.' identifier '(' expression_listopt ')'
    | primary_expression '[' expression ']'
    | exotic_call
    | closure_rule
;


closure_rule ::=
	closure
	| '*' identifier ;

closure ::=
	'{' '->' statements_noreset '}' ;

statements_noreset ::=
	  statements_noreset (?! Lreset) statement
	| (?! Lreset) statement
;


exotic_call ::=
	exotic_call_prefix '(' ')' ;

exotic_call_prefix ::=
	(?! '(' | '{') primary_expression '->' Linvoke ;

unary_expression (ExpressionNode) ::=
	primary_expression
	| '!' unary_expression
	| '-' unary_expression
;


%left '-' '+';
%left '*' '/';

binary_op (ExpressionNode) ::=
	unary_expression
	| binary_op '*' binary_op
	| binary_op '/' binary_op
	| binary_op '+' binary_op
	| binary_op '-' binary_op
;

conditional_expression ::=
    binary_op
  | binary_op '?' conditional_expression ':' conditional_expression
;

assignment_expression ::=
	conditional_expression
  | identifier '=' conditional_expression
;

expression ::=
	assignment_expression
  | expression ',' assignment_expression
;

expression_list ::=
	conditional_expression
	| expression_list ',' conditional_expression
;

%%

${template java.classcode-}
private static final boolean DEBUG_SYNTAX = true;
${end}
