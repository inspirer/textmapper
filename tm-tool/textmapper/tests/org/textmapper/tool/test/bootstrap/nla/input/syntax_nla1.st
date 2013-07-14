language syntax_nla1(java);

lang       = "java"
positions = "line,offset"
package	   = "com.mypackage"
breaks     = false

:: lexer

Lid(String):  /[a-zA-Z_][a-zA-Z_0-9]*/ -1	{ $lexem = new String(token,0,len); break; }
Lint:		  /[0-9]+/
_skip:        /\/\/.*/
_skip:        /[\t\r\n ]+/    { return false; }
'(':		  /\(/
')':		  /\)/
'=':		  /=/
';':		  /;/

Laaa:		 /aaa/
Lbbb:	 	 /bbb/

error:

:: parser

input ::=
	statements ;

statements ::=
	statements statement | statement ;

statement ::=
	expr
  | Laaa '=' error ';' { myprint("error in expression"); break; } ;

expr ::=
    '(' innerexpr ')'
  | Lint
  | Lid
;

innerexpr ::=
	(?! Lid) expr
  | Lbbb
;