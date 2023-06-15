language simple(go);

lang = "simple"
package = "github.com/inspirer/textmapper/parsers/simple"
eventBased = true

::lexer

WhiteSpace: /[\n\r\x20\t]+/ (space)

'simple': /simple/

'a': /a/
'b': /b/
'c': /c/

# See https://www.unicode.org/reports/tr31/tr31-37.html#Default_Identifier_Syntax
IDStart = /[_\p{L}\p{Nl}\p{Other_ID_Start}-\p{Pattern_Syntax}-\p{Pattern_White_Space}]/
IDFollow = /{IDStart}|[\p{Mn}\p{Mc}\p{Nd}\p{Pc}\p{Other_ID_Continue}-\p{Pattern_Syntax}-\p{Pattern_White_Space}]/

id: /\\{IDStart}{IDFollow}*/

::parser

%generate afterSimple = set(follow 'simple');

input : 'simple' (Xyz | .atB 'b') | Xyz+ .afterList | Foo+ .afterList | Bar+ ;
Foo : 'b' ;
Bar : 'a' ;
Xyz : 'c';