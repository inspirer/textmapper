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
'.': /\./
',': /,/
':': /:/
'-': /-/
'->': /->/

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
;

%flag Async = true;

delayed<Foo, Async> -> Delayed:
      '.' '.' expr
    | [Async && !Foo] '(' expr<~Foo> ')' '->' expr
;

%%

Simple :
  'a' expr<Foo="true"> -> File ;

expr<Foo> :
  Identifier -> Identifier -> Expr
| '(' expr<Foo="false"> ')' -> Bar -> Expr
| [Foo="true"] '{' (Identifier separator ',')+ '}' -> Init -> Expr
| delayed<Async="true",Foo=Foo> -> Expr
;

delayed<Foo, Async> :
  '.' '.' expr<Foo=Foo> -> Delayed
| [Async="true" | !(Foo="true")] '(' expr<Foo="false"> ')' '->' expr<Foo=Foo> -> Delayed
;

