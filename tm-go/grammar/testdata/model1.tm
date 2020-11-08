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
  'a' expr<Foo: "true"> -> File ;

expr<Foo> :
  Identifier -> Identifier -> Expr
| '(' expr<Foo: "false"> ')' -> Bar -> Expr
| [Foo="true"] '{' (Identifier separator ',')+ '}' -> Init -> Expr
| delayed<Foo: Foo, Async: "true"> -> Expr
| '->' delayed<Foo: Foo, Async: "true"> -> Expr
;

delayed<Foo, Async> :
  '.' '.' expr<Foo: Foo> -> Delayed
| [(Async="true" & !(Foo="true")) | Foo="true"] '(' expr<Foo: "false"> ')' '->' expr<Foo: Foo> -> Delayed
| '->' '(' PropagationWrap<IfFirst: "true"> ')' delayed<Foo: Foo, Async: Async> -> Delayed
;

PropagationWrap<IfFirst> :
  Propagation<IfFirst: IfFirst>
| '!' Propagation<IfFirst: "false">
;

Propagation<IfFirst> :
  '(' PropagationWrap<IfFirst: "false"> ')'
| [IfFirst="true"] (Propagation<IfFirst: IfFirst> | 'b') 'c'
| Propagation<IfFirst: IfFirst> '+' Propagation<IfFirst: "false">
| Propagation<IfFirst: "false"> '-' Propagation<IfFirst: "false">
| '-' Propagation<IfFirst: "false"> %prec UNO
;

