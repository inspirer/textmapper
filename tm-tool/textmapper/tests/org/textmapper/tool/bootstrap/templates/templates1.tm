language templates1(bison);

module = "templates1"

:: lexer

'=':        /=/
':':        /:/
'(':        /\(/
',':        /,/
')':        /\)/

a:  /a/
b:  /b/
c:  /c/
d:  /d/

:: parser

%flag A;
%flag B;
%flag C;

input ::=
	  DefaultP DefaultP<+C> DefaultP<+A,+C> DefaultP<+A,+B,+C>
;

# Test symbol propagation.
DefaultP<A,B,C> ::=
	  Terms
	| [A] d Terms<B>
;

Terms<A,B,C> ::=
	  [A && B && C] a b c
	| [A && !B && C] a c
	| [A && !B && !C] a
	| [A && B && !C] a b
	| [!A && B && C] b c
	| [!A && !B && C] c
	| [!A && B && !C] b
	| [!A && !B && !C]
;

