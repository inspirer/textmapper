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
  DefaultP1 DefaultP_C DefaultP_A_C DefaultP_A_B_C
;

Terms_B :
  b
;

Terms_A_B_C :
  a b c
;

DefaultP1 :
  Terms1
;

DefaultP_C :
  Terms_C
;

DefaultP_A_C :
  Terms_A_C
| d Terms1
;

DefaultP_A_B_C :
  Terms_A_B_C
| d Terms_B
;

Terms1 :
  %empty
;

Terms_C :
  c
;

Terms_A_C :
  a c
;

%%

