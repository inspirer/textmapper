language simple(go);

lang = "simple"
package = "github.com/inspirer/textmapper/tm-go/parsers/simple"
eventBased = true

::lexer

'simple' : /simple/

'a': /a/
'b': /b/
'c': /c/

::parser

%generate afterSimple = set(follow 'simple');

input : 'simple' (Xyz | .atB 'b') | Xyz+ .afterList | Foo+ .afterList | Bar+ ;
Foo : 'b' ;
Bar : 'a' ;
Xyz : 'c';