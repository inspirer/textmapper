language templates1(bison);

module = "templates1"

:: lexer

'=':        /=/
':':        /:/
'(':        /\(/
',':        /,/
')':        /\)/

'a':  /a/
'b':  /b/
'c':  /c/
'd':  /d/

:: parser

%explicit flag A = false;
%explicit flag B = false;
%explicit flag C = false;

input ::=
	  DefaultP DefaultP<+C> DefaultP<+A,+C> DefaultP<+A,+B,+C> IfThenElse<~C> IfThenElse<+C>
;

# Test 1: symbol propagation.
DefaultP<A,B,C> ::=
	  Terms<A,B,C>
	| [A] 'd' Terms<B>
;

# Test 2: rule conditions
Terms<A,B,C> ::=
	  [A && B && C] 'a' 'b' 'c'
	| [A && !B && C] 'a' 'c'
	| [A && !B && !C] 'a'
	| [A && B && !C] 'a' 'b'
	| [!A && B && C] 'b' 'c'
	| [!A && !B && C] 'c'
	| [!A && B && !C] 'b'
	| [!A && !B && !C]
;


# Test 3: templates and rule precedence.

%right 'd';

Condition<C> ::=
	  [C] 'a'
	| 'b'
;

IfThenElse<C> ::=
	  '(' Condition<C> ')' 'c' 'd' 'c'
	| '(' Condition<C> ')' 'c' %prec 'd'
;
