%{
#include <stdio.h>
%}

%start input

%right char_d
%token Assign
%token Colon
%token Lparen
%token Comma
%token Rparen
%token char_a
%token char_b
%token char_c

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
| char_d Terms_B
;

DefaultP_A_C :
  Terms_A_C
| char_d Terms
;

DefaultP_C :
  Terms_C
;

Terms :
  %empty
;

Terms_A_B_C :
  char_a char_b char_c
;

Terms_A_C :
  char_a char_c
;

Terms_B :
  char_b
;

Terms_C :
  char_c
;

Condition :
  char_b
;

Condition_C :
  char_a
| char_b
;

IfThenElse :
  Lparen Condition Rparen char_c char_d char_c
| Lparen Condition Rparen char_c %prec char_d
;

IfThenElse_C :
  Lparen Condition_C Rparen char_c char_d char_c
| Lparen Condition_C Rparen char_c %prec char_d
;

%%

