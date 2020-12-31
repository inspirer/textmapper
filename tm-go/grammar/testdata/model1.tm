language test(go);

:: lexer

Identifier: /[a-zA-Z_]+/    (class)

# Keywords.
'a':      /a/
'b':      /b/
'c':      /c/

# Punctuation
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'+': /\+/
'.': /\./
',': /,/
':': /:/
'-': /-/
'->': /->/
'!': /!/

UNO:

:: parser

%input Simple;
%flag Foo;


Simple -> File:
    ('-' | '+')+ 'a' expr<+Foo> '(' (('-' | '+') separator ',' ',')* ')' ;

%interface Expr;

expr<Foo> -> Expr:
    Identifier  -> Identifier
  | '(' expr<~Foo> ')' -> Bar
  | [Foo] '{' (Identifier separator ',')+ '}' -> Init
  | delayed<+Async>
  | '->' delayed
;

%flag Async = true;

delayed<Foo, Async> -> Delayed:
      '.' '.' expr
    | [Async && !Foo || Foo] '(' expr<~Foo> ')' '->' expr
    | '->' '(' PropagationWrap<+IfFirst> ')' delayed
;

%lookahead flag IfFirst;

PropagationWrap :
      Propagation
    | '!' Propagation
;

%left '+' '-';
%right UNO;

Propagation :
      '(' PropagationWrap ')'
    | [IfFirst] (Propagation | 'b') 'c'
    | Propagation '+' Propagation
    | Propagation<~IfFirst> '-' Propagation
    | '-' Propagation %prec UNO
;

%%

Simple :
  Simple$1 CHAR_A expr_Foo LPAREN Simple$2opt RPAREN -> File
;

Simple$1 :
  Simple$1 MINUS
| Simple$1 PLUS
| MINUS
| PLUS
;

Simple$2 :
  Simple$2 COMMA COMMA MINUS
| Simple$2 COMMA COMMA PLUS
| MINUS
| PLUS
;

Simple$2opt :
  Simple$2
| %empty
;

expr :
  IDENTIFIER -> Identifier -> Expr
| LPAREN expr RPAREN -> Bar -> Expr
| delayed_Async -> Expr
| MINUSGT delayed_Async -> Expr
;

expr_Foo :
  IDENTIFIER -> Identifier -> Expr
| LPAREN expr RPAREN -> Bar -> Expr
| LBRACE Identifier_list_Comma_separated RBRACE -> Init -> Expr
| delayed_Foo_Async -> Expr
| MINUSGT delayed_Foo_Async -> Expr
;

Identifier_list_Comma_separated :
  Identifier_list_Comma_separated COMMA IDENTIFIER
| IDENTIFIER
;

delayed_Async :
  DOT DOT expr -> Delayed
| LPAREN expr RPAREN MINUSGT expr -> Delayed
| MINUSGT LPAREN PropagationWrap_IfFirst RPAREN delayed_Async -> Delayed
;

delayed_Foo_Async :
  DOT DOT expr_Foo -> Delayed
| LPAREN expr RPAREN MINUSGT expr_Foo -> Delayed
| MINUSGT LPAREN PropagationWrap_IfFirst RPAREN delayed_Foo_Async -> Delayed
;

PropagationWrap :
  Propagation
| EXCL Propagation
;

PropagationWrap_IfFirst :
  Propagation_IfFirst
| EXCL Propagation
;

Propagation :
  LPAREN PropagationWrap RPAREN
| Propagation PLUS Propagation
| Propagation MINUS Propagation
| MINUS Propagation %prec UNO
;

Propagation_IfFirst :
  LPAREN PropagationWrap RPAREN
| Propagation_IfFirst CHAR_C
| CHAR_B CHAR_C
| Propagation_IfFirst PLUS Propagation
| Propagation MINUS Propagation
| MINUS Propagation %prec UNO
;

