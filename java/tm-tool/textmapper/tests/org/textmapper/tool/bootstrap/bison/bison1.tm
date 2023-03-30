language bison1(bison);

module = "bison1"

:: lexer

identifier: /[a-zA-Z_][a-zA-Z_0-9]*/ (class)
icon:       /-?[0-9]+/
scon:       /"([^\n\\"]|\\.)*"/

# soft keyword

kw_object: /object/

skip: /[\n\t\r ]+/        (space)
comment:  /#.*(\r?\n)?/   (space)

'=':        /=/
':':        /:/
'(':        /\(/
',':        /,/
')':        /\)/

a1:  /a1/
a2:  /a2/
a3:  /a3/
a4:  /a4/

:: parser

%left a1 a2;
%right a3;
%nonassoc a4;

input :
	  list<Of: assignment>
			epilogue<T: a1, +AllowObject>
			epilogue<T: a2, ~AllowObject>
;

%global param T;

epilogue :
	  T '(' list<Of: expression> ')'
;

assignment :
	  (identifier | kw_object) '='? object				{ $$ = combine($0, $2); }
;

object :
	  kw_object ('(' list<Of: key_value>  ')')?
;

key_value :
	  icon  ':' sconopt
;

list<param Of> :  (Of separator ',')+ ;

%global flag AllowObject = false;

expression :
	  icon
	| [AllowObject] object
;