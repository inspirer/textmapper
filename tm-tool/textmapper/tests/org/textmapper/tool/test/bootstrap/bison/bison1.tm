language bison1(bison);

module = "bison1"

:: lexer

identifier: /[a-zA-Z_][a-zA-Z_0-9]*/ (class)
icon:       /-?[0-9]+/
scon:       /"([^\n\\"]|\\.)*"/

# soft keyword

kw_object: /object/       (soft)

skip: /[\n\t\r ]+/        (space)
comment:  /#.*(\r?\n)?/   (space)

'=':        /=/
':':        /:/
'(':        /\(/
',':        /,/
')':        /\)/

:: parser

input ::=
	  list<assignment>
;

assignment ::=
	  identifier '='? object				{ $$ = combine($0, $2); }
;

object ::=
	  kw_object ('(' list<key_value>  ')')?
;

key_value ::=
	  icon  ':' scon
;

%param T symbol;

list<T> ::=  (T separator ',')+ ;
