language rewrite(java);

prefix = "Rewrite"
package = "org.textmapper.tool.compiler.input"

:: lexer

'{':	/\\/
'}':	/\}/
',':	/,/
'.':	/\./
'a':	/a/
'b':	/b/

:: parser

input ::= ;

Elem ::= '{' | '}' ;
Elem2 ::= 'a' ;
Elem3 ::= 'b' ;


# test: (Elem)+
ElemPlus1 ::=
	  Elem
	| ElemPlus1 Elem
;

# test: (Elem /rr)+
ElemPlus1rr ::=
	  Elem
	| Elem ElemPlus1rr
;

# test: (Elem separator '.')+
ElemPlus2 ::=
	  Elem
	| ElemPlus2 '.' Elem
;

# test: (Elem separator '.' /rr)+
ElemPlus2rr ::=
	  Elem
	| Elem '.' ElemPlus2rr
;

# test: (Elem separator ('.' '.'))+
ElemPlus3 ::=
	  Elem
	| ElemPlus3 '.' '.' Elem
;

# test: (Elem (('.' '.') (Elem | Elem2 | Elem3))*)
ElemPlus4 ::=
	  Elem
	| ElemPlus4 '.' '.' Elem
	| ElemPlus4 '.' '.' Elem2
	| ElemPlus4 '.' '.' Elem3
;

# test: ((Elem | Elem2 | Elem3) separator ('.' '.'))+
ElemPlus5 ::=
	  Elem
	| Elem2
	| Elem3
	| ElemPlus5 '.' '.' Elem
	| ElemPlus5 '.' '.' Elem2
	| ElemPlus5 '.' '.' Elem3
;

# test: (Elem)*
ElemStar1 ::=
	| Elem
	| ElemStar1 Elem
;

# test: ((Elem2 | Elem3))*
ElemStar1ex ::=
	| ElemStar1ex Elem2
	| ElemStar1ex Elem3
;

# test: (Elem)*
ElemStar2 ::=
	| ElemStar2 Elem
;

# test: (Elem /rr)*
ElemStar3rr ::=
	| Elem ElemStar3rr
;

# test: (Elem /rr)*
ElemStar4rr ::=
	| Elem
	| Elem ElemStar4rr
;

# test: (Elem separator ('{' '}') /rr)+
ElemPlus5rr ::=
	Elem
	| Elem '{' '}' ElemPlus5rr
;

# test: (Elem ('{' '}') /rr)*
ElemSep6rr ::=
	| Elem '{' '}' ElemSep6rr
;

# TODO: fix; actual = ((EStar1$1 /rr)* (Elem | ()))
EStar1 ::=
	| Elem
	| (Elem |  )  EStar1
;

# TODO: fix; actual = (((Elem)? /rr)* (Elem | ()))
EStar2 ::=
	| Elem
	| Elem? EStar2
;


#ElemPlus5rr_ ::=
#	Elem
#	| Elem '{' '}' ElemPlus5rr
#;
