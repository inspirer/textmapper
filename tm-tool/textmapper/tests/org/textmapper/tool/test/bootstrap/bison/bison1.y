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
  list_of_assignment
;

assignment :
  identifier Equal object
			{ $$ = combine($0, $2); }
| identifier object
			{ $$ = combine($0, $1); }
;

object :
  kw_object Lparen list_of_key_value Rparen
| kw_object
;

key_value :
  icon Colon sconopt
;

key_value_list_Comma_separated :
  key_value_list_Comma_separated Comma key_value
| key_value
;

sconopt :
  %empty
| scon
;

list_of_key_value :
  key_value_list_Comma_separated
;

list_of_assignment :
  assignment_list_Comma_separated
;

assignment_list_Comma_separated :
  assignment_list_Comma_separated Comma assignment
| assignment
;

%%

