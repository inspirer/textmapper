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
    'a' expr<+Foo>
;

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
  'a' expr_Foo -> File ;

expr :
  Identifier -> Identifier -> Expr
| '(' expr ')' -> Bar -> Expr
| delayed_Async -> Expr
| '->' delayed_Async -> Expr
;

expr_Foo :
  Identifier -> Identifier -> Expr
| '(' expr ')' -> Bar -> Expr
| '{' (Identifier separator ',')+ '}' -> Init -> Expr
| delayed_Foo_Async -> Expr
| '->' delayed_Foo_Async -> Expr
;

delayed_Async :
  '.' '.' expr -> Delayed
| '(' expr ')' '->' expr -> Delayed
| '->' '(' PropagationWrap_IfFirst ')' delayed_Async -> Delayed
;

delayed_Foo_Async :
  '.' '.' expr_Foo -> Delayed
| '(' expr ')' '->' expr_Foo -> Delayed
| '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async -> Delayed
;

PropagationWrap :
  Propagation
| '!' Propagation
;

PropagationWrap_IfFirst :
  Propagation_IfFirst
| '!' Propagation
;

Propagation :
  '(' PropagationWrap ')'
| Propagation '+' Propagation
| Propagation '-' Propagation
| '-' Propagation %prec UNO
;

Propagation_IfFirst :
  '(' PropagationWrap ')'
| (Propagation_IfFirst | 'b') 'c'
| Propagation_IfFirst '+' Propagation
| Propagation '-' Propagation
| '-' Propagation %prec UNO
;

