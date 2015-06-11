input :
	list_T-assignment
;

assignment :
	identifier '=' object
			{ $$ = combine($0, $2); }
  | identifier object
			{ $$ = combine($0, $1); }
;

object :
	kw_object '(' list_T-key_value ')'
  | kw_object
;

key_value :
	icon ':' scon
;

null_T-key_value :
	null_T-key_value ',' key_value
  | key_value
;

list_T-key_value :
	null_T-key_value
;

list_T-assignment :
	null_T-assignment
;

null_T-assignment :
	null_T-assignment ',' assignment
  | assignment
;

