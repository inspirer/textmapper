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
flexMode = true

:: lexer

'{': /\{/
'}': /\}/
'[': /\[/
']': /\]/
':': /:/
',': /,/  // comma

MultiLineComment: (space)
// not a trailing comment

JSONString:   // "string literal"
JSONNumber:

id:
kw_null:
'true':
'false':

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
    JSONString ':' JSONValue<~A> { $$ = a; }
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
