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
  list_listDollarOf_assignment epilogue_AllowObject_T_a1 epilogue_T_a2
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

list_listDollarOf_key_value1 :
  key_value_list_Comma_separated2
;

object2 :
  kw_object Lparen list_listDollarOf_key_value1 Rparen
| kw_object
;

sconopt :
  %empty
| scon
;

list_AllowObject_listDollarOf_key_value :
  key_value_list_Comma_separated1
;

list_listDollarOf_assignment :
  assignment_list_Comma_separated
;

epilogue_AllowObject_T_a1 :
  a1 Lparen list_AllowObject_listDollarOf_expression Rparen
;

epilogue_T_a2 :
  a2 Lparen list_listDollarOf_expression Rparen
;

assignment_list_Comma_separated :
  assignment_list_Comma_separated Comma assignment
| assignment
;

list_AllowObject_listDollarOf_expression :
  expression_AllowObject_list_Comma_separated
;

list_listDollarOf_expression :
  expression_list_Comma_separated
;

expression_AllowObject_list_Comma_separated :
  expression_AllowObject_list_Comma_separated Comma expression_AllowObject
| expression_AllowObject
;

expression_list_Comma_separated :
  expression_list_Comma_separated Comma expression1
| expression1
;

object1 :
  kw_object Lparen list_listDollarOf_key_value Rparen
| kw_object
;

expression_AllowObject :
  icon
| object_AllowObject
;

expression1 :
  icon
;

list_listDollarOf_key_value :
  key_value_list_Comma_separated
;

object_AllowObject :
  kw_object Lparen list_AllowObject_listDollarOf_key_value Rparen
| kw_object
;

key_value_list_Comma_separated :
  key_value_list_Comma_separated Comma key_value
| key_value
;

%%

