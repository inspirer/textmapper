%{
#include <stdio.h>
%}

%start input

%left a1 a2
%right a3
%nonassoc a4
%token identifier
%token icon
%token scon
%token kw_object
%token skip
%token comment
%token Equal
%token Colon
%token Lparen
%token Comma
%token Rparen

%locations
%%

input :
  list_TMinusassignment
;

assignment :
  identifier Equal object
			{ $$ = combine($0, $2); }
| identifier object
			{ $$ = combine($0, $1); }
;

object :
  kw_object Lparen list_TMinuskey_value Rparen
| kw_object
;

key_value :
  icon Colon sconopt
;

null_TMinuskey_value :
  null_TMinuskey_value Comma key_value
| key_value
;

sconopt :
  %empty
| scon
;

list_TMinuskey_value :
  null_TMinuskey_value
;

list_TMinusassignment :
  null_TMinusassignment
;

null_TMinusassignment :
  null_TMinusassignment Comma assignment
| assignment
;

%%

