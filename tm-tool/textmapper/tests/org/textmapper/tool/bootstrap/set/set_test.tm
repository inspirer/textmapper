language set_test(java);

prefix = "Set"
package = "org.textmapper.tool.bootstrap.set"
breaks = true
gentree = false
genast = false
genastdef = true
positions = "line,offset"
endpositions = "offset"

:: lexer

'a': /a/
'b': /b/
'c': /c/
'd': /d/
'e': /e/
'f': /f/
'g': /g/
'h': /h/
'i': /i/


'3': /3/
'4': /4/
'5': /5/
'6': /6/

:: parser

input ::=
	  abcdef+ test_3
;

abcdef ::=
	  set(first pair) set(pair) set('h') # set(follow 'a')
    test2
;

pair ::=
	  'a' 'b'
	| 'c' 'd'
	| 'e' 'f'
;

# test 2

test2 ::=
	  set(first recursive) set(recursive)
;

recursive ::=
	  'b' 'a'
	| helper
;

helper ::=
	  'i' recursive 'h' 'i'
;

# test 3

%generate precede4 = set(precede '4');

test_3 ::=
	  '3' test_3_helper '4' '5'
;

test_3_helper ::=
	('b' 'c'? maybeD)?
;

maybeD ::= 'd'? ;
