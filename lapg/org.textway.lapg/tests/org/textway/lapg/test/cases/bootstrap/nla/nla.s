# lapg syntax file

lang = "java"
prefix = "NlaTest"
package = "org.textway.lapg.test.cases.bootstrap.nla"
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/ (class)   { $lexem = current(); break; }
icon(Integer):  /-?[0-9]+/                     { $lexem = Integer.parseInt(current()); break; }
_skip:          /[\n\t\r ]+/                   { return false; }

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
