%{
#include <stdio.h>
%}

%start input

%token Equal
%token Colon
%token Lparen
%token Comma
%token Rparen
%token a
%token b
%token c
%token d

%locations
%%

input :
  DefaultP DefaultP_C DefaultP_A_C DefaultP_A_B_C
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

%%

