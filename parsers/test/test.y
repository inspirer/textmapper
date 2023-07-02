%{
%}

%start FooLookahead // no-eoi
%start Test
%start Decl1

%nonassoc AS
%left PLUS
%token INVALID_TOKEN
%token WHITESPACE
%token SINGLELINECOMMENT
%token IDENTIFIER
%token IDENTIFIER2
%token INTEGERCONSTANT
%token LASTINT
%token TEST
%token DECL1
%token DECL2
%token EVAL
%token IF
%token ELSE
%token LBRACE
%token RBRACE
%token LPAREN
%token RPAREN
%token LBRACK
%token RBRACK
%token DOT
%token DOTDOTDOT
%token COMMA
%token COLON
%token MINUS
%token MINUSGT
%token ESC
%token CHAR__
%token FOO_
%token F_A
%token MULTILINE
%token DQUOTE
%token APOS
%token SHARPATID
%token ZFOO
%token BACKTRACKINGTOKEN
%token ERROR
%token MULTILINECOMMENT

%%

Declaration_list :
  Declaration_list Declaration
| Declaration
;

Test :
  Declaration_list
;

Declaration :
  Decl1
| Decl2
| LBRACE MINUS MINUS Declaration_list RBRACE
| LBRACE MINUS MINUS RBRACE
| LBRACE MINUS Declaration_list RBRACE
| LBRACE MINUS RBRACE
| LBRACE Declaration_list RBRACE
| LBRACE RBRACE
| LASTINT
			{ println("it works") }
| INTEGERCONSTANT LBRACK RBRACK
			{
        switch (/*int*/$0) {
        case 7:
          p.listener(Int7, 0, @0.offset, @2.endoffset)
        case 9:
          p.listener(Int9, 0, @0.offset, @2.endoffset)
        }
      }
| INTEGERCONSTANT
			{
        switch (/*int*/$0) {
        case 7:
          p.listener(Int7, 0, @0.offset, @0.endoffset)
        case 9:
          p.listener(Int9, 0, @0.offset, @0.endoffset)
        }
      }
| TEST LBRACE setof_not_EOI_or_DOT_or_RBRACE_optlist RBRACE
| TEST LPAREN empty1 RPAREN
| TEST LPAREN foo_nonterm RPAREN
| TEST INTEGERCONSTANT
| EVAL lookahead_notFooLookahead LPAREN expr RPAREN empty1
| EVAL lookahead_FooLookahead LPAREN foo_nonterm_A RPAREN
| EVAL lookahead_FooLookahead LPAREN INTEGERCONSTANT DOT expr PLUS /*.greedy*/ expr RPAREN
| DECL2 COLON QualifiedNameopt
;

// lookahead: FooLookahead
lookahead_FooLookahead :
  %empty
;

// lookahead: !FooLookahead
lookahead_notFooLookahead :
  %empty
;

setof_not_EOI_or_DOT_or_RBRACE :
  INVALID_TOKEN
| WHITESPACE
| SINGLELINECOMMENT
| IDENTIFIER
| IDENTIFIER2
| INTEGERCONSTANT
| LASTINT
| TEST
| DECL1
| DECL2
| EVAL
| AS
| IF
| ELSE
| LBRACE
| LPAREN
| RPAREN
| LBRACK
| RBRACK
| DOTDOTDOT
| COMMA
| COLON
| MINUS
| MINUSGT
| PLUS
| ESC
| CHAR__
| FOO_
| F_A
| MULTILINE
| DQUOTE
| APOS
| SHARPATID
| ZFOO
| BACKTRACKINGTOKEN
| ERROR
| MULTILINECOMMENT
;

setof_not_EOI_or_DOT_or_RBRACE_optlist :
  setof_not_EOI_or_DOT_or_RBRACE_optlist setof_not_EOI_or_DOT_or_RBRACE
| %empty
;

FooLookahead :
  LPAREN INTEGERCONSTANT DOT setof_foo_la_list RPAREN
;

setof_foo_la :
  INTEGERCONSTANT
| AS
| DOT
| PLUS
| ESC
| FOO_
;

setof_foo_la_list :
  setof_foo_la_list setof_foo_la
| setof_foo_la
;

empty1 :
  %empty
;

foo_la :
  INTEGERCONSTANT DOT expr
| INTEGERCONSTANT FOO_ expr
;

foo_nonterm :
  INTEGERCONSTANT DOT expr
;

foo_nonterm_A :
  INTEGERCONSTANT DOT expr
| INTEGERCONSTANT FOO_ expr
;

QualifiedName :
  IDENTIFIER
| QualifiedName DOT IDENTIFIER
;

Decl1 :
  DECL1 LPAREN QualifiedName RPAREN
;

Decl2 :
  DECL2
| If
;

If :
  IF LPAREN RPAREN Decl2
| IF LPAREN RPAREN Decl2 ELSE Decl2
;

expr :
  expr PLUS primaryExpr
| customPlus
| primaryExpr
;

customPlus :
  ESC primaryExpr PLUS expr
			{ p.listener(PlusExpr, 0, @0.offset, @3.endoffset) }
;

primaryExpr :
  primaryExpr_WithoutAs AS expr
| INTEGERCONSTANT
;

primaryExpr_WithoutAs :
  INTEGERCONSTANT
;

QualifiedNameopt :
  QualifiedName
| %empty
;

%%

