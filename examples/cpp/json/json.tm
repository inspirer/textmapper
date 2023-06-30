language json(cc);

namespace = "json"
includeGuardPrefix = "EXAMPLES_JSON_"

:: lexer

'{': /\{/
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

kw_null: /null/
'true': /true/
'false': /false/

'A': /A/
'B': /B/

error:
invalid_token:

:: parser

%input JSONText;

%generate Literals = set(first JSONValue<+A>);

%flag A;

JSONText -> JSONText :
    JSONValue<+A> ;

JSONValue<A> {Value} -> JSONValue :
    kw_null
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

EmptyObject -> EmptyObject : (?= EmptyObject) '{' '}' ;

JSONObject -> JSONObject :
    (?= !EmptyObject) '{' JSONMemberList? '}' ;

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
