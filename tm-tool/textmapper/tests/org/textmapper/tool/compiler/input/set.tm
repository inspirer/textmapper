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
j: /j/

:: parser

input ::=
	  aa efgh bb qq
	  bb_all bb_first bb_last bb_qq all
	  negated negated2 expr
	  bb_follow bb_all_precede
	  use_j_2 j_precede j_follow ;

# [a, b, c, d, e, f, g, i, j]
aa ::= set(~h & ~eoi) ;

# [e, f, g, h]
efgh ::= set(e | f | g | h);

# [a, b, c, d, i, j]
bb ::= set(aa & ~(efgh)) ;

# [a, b, c, d, i, j]
bb_all ::= set(bb) ;

# [a, b, c, d, i, j]
bb_first ::= set(first bb) ;

# [a, b, c, d, i, j]
bb_last ::= set(last bb) ;

# [e]
qq ::= set(e) ;

# [a, b, c, d, e, i, j]
bb_qq ::= set(bb | qq);

# [a, b, c, d, i, j]
all ::= set(aa & bb & bb_qq & ~eoi) ;

# [a, b, c, d, e, f, g, h, i, j]
negated ::= set(~eoi) ;

# [a, b, c, d, e, eoi, f, g, h, j]
negated2 ::= set(~i) ;

# [b, d, e]
expr ::= set(bb & (b | d | h) | qq) ;

# [e]
bb_follow ::= set(follow bb);

# [e]
bb_all_precede ::= set(precede bb_all);

use_j_2 ::= a b c use_j qq=d as false? f ;
use_j ::= e? a a=b? (e as true? f | d j f)? j | j ;

# [d, f]
j_follow ::= set(follow j);

# [a, b, c, d, f]
j_precede ::= set(precede j);
