language json(cc);

namespace = "json"
includeGuardPrefix = "EXAMPLES_JSON_"
tokenLineOffset = true
tokenColumn = true
filenamePrefix = "json_"
optimizeTables = true
eventBased = true
extraTypes = ["NonExistingType"]
parseParams = ["int a", "bool b"]
debugParser = true
scanBytes = true

:: lexer

%s initial, foo;

'{': /\{/
'}': /\}/
'[': /\[/
']': /\]/
':': /:/
',': /,/

<foo> Foo: /\#/

space: /[\t\r\n ]+/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment: /\/\*{commentChars}\*\// (space)

hex = /[0-9a-fA-F]/

# TODO
JSONString: /"([^"\\]|\\(["\/\\bfnrt]|u{hex}{4}))*"/
#JSONString: /"([^"\\\x00-\x1f]|\\(["\/\\bfnrt]|u{hex}{4}))*"/

fraction = /\.[0-9]+/
exp = /[eE][+-]?[0-9]+/
JSONNumber: /-?(0|[1-9][0-9]*){fraction}?{exp}?/

id: /[a-zA-Z][a-zA-Z0-9]*/ (class)

kw_null: /null/
'true': /true/
'false': /false/

'A': /A/
'A': /α/
'B': /B/

'A': /A!/ { /*some code */ }

error:
invalid_token:

:: parser

%input JSONText;

%inject MultiLineComment -> MultiLineComment/Bar,Foo;
%inject invalid_token -> InvalidToken;
%inject JSONString -> JsonString;

%generate Literals = set(first JSONValue<+A>);

%flag A;

JSONText {bool b} -> JSONText :
    JSONValue<+A>[val] { $$ = $val; } ;

JSONValue<A> {int a} -> JSONValue :
    kw_null
  | 'true'
  | 'false'    { $$ = 5; }
  | [A] 'A'
  | [!A] 'B'
  | JSONObject
  | EmptyObject
  | JSONArray
  | JSONString
  | JSONNumber
;

EmptyObject -> EmptyObject : (?= EmptyObject) '{' '}' { @$.begin = @2.begin; } ;

JSONObject -> JSONObject/Foo :
    (?= !EmptyObject) '{' JSONMemberList? '}' { @$.begin = @2.begin; } ;

JSONMember {int c} -> JSONMember/Foo :
    JSONString ':'[b] { LOG(INFO) << @b.begin; } JSONValue<~A> { $$ = a; }
  | error -> SyntaxProblem
;

JSONMemberList {bool d}:
    JSONMember  { $$ = b; }
  | JSONMemberList .foo ',' JSONMember
;

JSONArray -> JSONArray/Foo :
    .bar '[' JSONElementListopt ']' ;

JSONElementList :
    JSONValue<+A>
  | JSONElementList ',' JSONValue<+A>
;
