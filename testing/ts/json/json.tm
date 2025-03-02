language json(ts);

tokenLine = true
eventBased = true
genSelector = true
fixWhitespace = true
extraTypes = ["NonExistingType"]

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

'null': /null/
'true': /true/
'false': /false/

error:
invalid_token:

:: parser

%input JSONText;

%inject MultiLineComment -> MultiLineComment;
%inject invalid_token -> InvalidToken;
%inject JSONString -> JSONString;

%generate Literals = set(first JSONValue);

JSONText -> JSONText :
    JSONValue ;

JSONValue -> JSONValue :
    'null'
  | 'true'
  | 'false'
  | JSONObject
  | JSONArray
  | JSONString
  | JSONNumber
;

JSONObject -> JSONObject :
    '{' JSONMemberList? '}' ;

JSONMember -> JSONMember :
    JSONString ':' JSONValue ;

JSONMemberList :
    JSONMember
  | JSONMemberList ',' JSONMember
;

JSONArray -> JSONArray :
    '[' JSONElementListopt ']' ;

JSONElementList :
    JSONValue
  | JSONElementList ',' JSONValue
;
