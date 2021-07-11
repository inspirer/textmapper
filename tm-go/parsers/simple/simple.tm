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

input : 'simple' | Xyz+ | Foo+ | Bar+ ;
Foo : 'b' ;
Bar : 'a' ;
Xyz : 'c';