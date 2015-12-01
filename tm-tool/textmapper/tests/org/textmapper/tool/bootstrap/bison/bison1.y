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
  list_of_assignment epilogue_AllowObject_a1 epilogue_nonAllowObject_a2
;

key_value_list_Comma_separated2 :
  key_value_list_Comma_separated2 Comma key_value
| key_value
;

assignment :
  identifier Equal object1
			{ $$ = combine($0, $2); }
| identifier object1
			{ $$ = combine($0, $1); }
;

key_value_list_Comma_separated1 :
  key_value_list_Comma_separated1 Comma key_value
| key_value
;

key_value :
  icon Colon sconopt
;

list_of_nonAllowObject_key_value :
  key_value_list_Comma_separated2
;

object_nonAllowObject :
  kw_object Lparen list_of_nonAllowObject_key_value Rparen
| kw_object
;

sconopt :
  %empty
| scon
;

list_of_AllowObject_key_value :
  key_value_list_Comma_separated1
;

epilogue_AllowObject_a1 :
  a1 Lparen list_of_AllowObject_expression Rparen
;

list_of_assignment :
  assignment_list_Comma_separated
;

epilogue_nonAllowObject_a2 :
  a2 Lparen list_of_nonAllowObject_expression Rparen
;

assignment_list_Comma_separated :
  assignment_list_Comma_separated Comma assignment
| assignment
;

list_of_AllowObject_expression :
  expression_AllowObject_list_Comma_separated
;

list_of_nonAllowObject_expression :
  expression_nonAllowObject_list_Comma_separated
;

expression_AllowObject_list_Comma_separated :
  expression_AllowObject_list_Comma_separated Comma expression_AllowObject
| expression_AllowObject
;

expression_nonAllowObject_list_Comma_separated :
  expression_nonAllowObject_list_Comma_separated Comma expression_nonAllowObject
| expression_nonAllowObject
;

object1 :
  kw_object Lparen list_of_key_value Rparen
| kw_object
;

expression_AllowObject :
  icon
| object_AllowObject
;

expression_nonAllowObject :
  icon
;

list_of_key_value :
  key_value_list_Comma_separated
;

object_AllowObject :
  kw_object Lparen list_of_AllowObject_key_value Rparen
| kw_object
;

key_value_list_Comma_separated :
  key_value_list_Comma_separated Comma key_value
| key_value
;

%%

