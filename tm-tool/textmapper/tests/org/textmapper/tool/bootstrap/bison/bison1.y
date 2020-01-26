%{
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
%token Assign
%token Colon
%token Lparen
%token Comma
%token Rparen

%%

input :
  list_Of_assignment epilogue_AllowObject_T_a1 epilogue_T_a2
;

epilogue_AllowObject_T_a1 :
  a1 Lparen list_AllowObject_Of_expression Rparen
;

epilogue_T_a2 :
  a2 Lparen list_Of_expression Rparen
;

assignment :
  identifier Assign object
			{ $$ = combine($0, $1); }
| identifier object
			{ $$ = combine($0, null); }
| kw_object Assign object
			{ $$ = combine(null, $1); }
| kw_object object
			{ $$ = combine(null, null); }
;

object :
  kw_object Lparen list_Of_key_value Rparen
| kw_object
;

object_AllowObject :
  kw_object Lparen list_AllowObject_Of_key_value Rparen
| kw_object
;

key_value :
  icon Colon sconopt
;

assignment_list_Comma_separated :
  assignment_list_Comma_separated Comma assignment
| assignment
;

expression_AllowObject_list_Comma_separated :
  expression_AllowObject_list_Comma_separated Comma expression_AllowObject
| expression_AllowObject
;

expression_list_Comma_separated :
  expression_list_Comma_separated Comma expression
| expression
;

key_value_list_Comma_separated :
  key_value_list_Comma_separated Comma key_value
| key_value
;

key_value_list_Comma_separated1 :
  key_value_list_Comma_separated1 Comma key_value
| key_value
;

list_AllowObject_Of_expression :
  expression_AllowObject_list_Comma_separated
;

list_AllowObject_Of_key_value :
  key_value_list_Comma_separated1
;

list_Of_assignment :
  assignment_list_Comma_separated
;

list_Of_expression :
  expression_list_Comma_separated
;

list_Of_key_value :
  key_value_list_Comma_separated
;

expression :
  icon
;

expression_AllowObject :
  icon
| object_AllowObject
;

sconopt :
  %empty
| scon
;

%%

