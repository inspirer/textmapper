language set_test(java);

prefix = "Set"
package = "org.textmapper.tool.test.bootstrap.set"
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

:: parser

input ::=
	  abcdef+
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
