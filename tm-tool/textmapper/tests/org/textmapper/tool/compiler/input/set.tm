language set_tm(null);

:: lexer

eoi:

a: /a/
b: /b/
c: /c/
d: /d/
e: /e/
f: /f/
g: /g/
h: /h/
i: /i/

:: parser

input ::=
	  aa efgh bb qq
	  bb_all bb_first bb_qq all
	  negated negated2 expr ;

# [a, b, c, d, e, f, g, i]
aa ::= set(~h & ~eoi) ;

# [e, f, g, h]
efgh ::= set(e | f | g | h);

# [a, b, c, d, i]
bb ::= set(aa & ~(efgh)) ;

# [a, b, c, d, i]
bb_all ::= set(bb) ;

# [a, b, c, d, i]
bb_first ::= set(first bb) ;

# [e]
qq ::= set(e) ;

# [a, b, c, d, e, i]
bb_qq ::= set(bb | qq);

# [a, b, c, d, i]
all ::= set(aa & bb & bb_qq & ~eoi) ;

# [a, b, c, d, e, f, g, h, i]
negated ::= set(~eoi) ;

# [a, b, c, d, e, eoi, f, g, h]
negated2 ::= set(~i) ;

# [b, d, e]
expr ::= set(bb & (b | d | h) | qq) ;
