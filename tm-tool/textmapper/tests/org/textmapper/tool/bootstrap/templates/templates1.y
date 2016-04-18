%{
#include <stdio.h>
%}

%start input

%right d
%token Assign
%token Colon
%token Lparen
%token Comma
%token Rparen
%token a
%token b
%token c

%locations
%%

input :
  DefaultP DefaultP_C DefaultP_A_C DefaultP_A_B_C IfThenElse IfThenElse_C
;

DefaultP :
  Terms
;

DefaultP_A_B_C :
  Terms_A_B_C
| d Terms_B
;

DefaultP_A_C :
  Terms_A_C
| d Terms
;

DefaultP_C :
  Terms_C
;

Terms :
  %empty
;

Terms_A_B_C :
  a b c
;

Terms_A_C :
  a c
;

Terms_B :
  b
;

Terms_C :
  c
;

Condition :
  b
;

Condition_C :
  a
| b
;

IfThenElse :
  Lparen Condition Rparen c d c
| Lparen Condition Rparen c %prec d
;

IfThenElse_C :
  Lparen Condition_C Rparen c d c
| Lparen Condition_C Rparen c %prec d
;

%%

