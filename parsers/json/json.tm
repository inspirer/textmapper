language json(go);

lang = "json"
package = "github.com/inspirer/textmapper/parsers/json"
eventBased = true
optimizeTables = true
extraTypes = ["NonExistingType"]

:: lexer

'{' {int64}: /\{/  { $$ = int64(42); }
'}': /\}/
'[': /\[/
']': /\]/
':': /:/
',': /,/

space: /[\t\r\n ]+/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment: /\/\*{commentChars}\*\// (space)

hex = /[0-9a-fA-F]/

# TODO
JSONString {string}: /"([^"\\]|\\(["\/\\bfnrt]|u{hex}{4}))*"/
#JSONString: /"([^"\\\x00-\x1f]|\\(["\/\\bfnrt]|u{hex}{4}))*"/

fraction = /\.[0-9]+/
exp = /[eE][+-]?[0-9]+/
JSONNumber: /-?(0|[1-9][0-9]*){fraction}?{exp}?/

id: /[a-zA-Z][a-zA-Z0-9]*/ (class)

'null': /null/
'true': /true/
'false': /false/

'A': /A/
'B': /B/

error:
invalid_token:

:: parser

%input JSONText;

%inject MultiLineComment -> MultiLineComment;
%inject invalid_token -> InvalidToken;
%inject JSONString -> JSONString;

%generate Literals = set(first JSONValue<+A>);

%flag A;

JSONText -> JSONText :
    JSONValue<+A> ;

JSONValue<A> {Value} -> JSONValue :
    'null'
  | 'true'
  | 'false'
  | [A] 'A'
  | [!A] 'B'
  | JSONObject
  | EmptyObject
  | JSONArray
  | JSONString
  | JSONNumber
;

EmptyObject -> EmptyObject :
      (?= EmptyObject)
         { /* empty mid-rule */ }
      '{'[lparen] { val := $lparen; if val != int64(42) { panic(fmt.Sprintf("got %v %T", val, val)) } } '}'
      { val := $lparen; _ = val }
;

JSONObject -> JSONObject :
    (?= !EmptyObject) '{'[F] JSONMemberList? { /*mid-rule ${F.offset}*/ } '}' { /*starts ${F.offset}*/ } ;

JSONMember {*Field} -> JSONMember :
    JSONString ':' JSONValue<~A> ;

JSONMemberList :
    JSONMember
  | JSONMemberList ',' JSONMember
;

JSONArray -> JSONArray :
    '[' JSONElementListopt ']' ;

JSONElementList :
    JSONValue<+A>
  | JSONElementList ',' JSONValue<+A>
;
