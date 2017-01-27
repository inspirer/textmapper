language json(cpp);

prefix = "json"
namespace = "parsers::json"
positions = "offset"
endpositions = "offset"

:: lexer

'{': /\{/
'}': /\}/
'[': /\[/
']': /\]/
':': /:/
',': /,/

space: /[\t\r\n ]+/ (space)

hex = /[0-9a-fA-F]/

# TODO
JSONString: /"([^"\\]|\\(["\/\\bfnrt]|u{hex}{4}))*"/
#JSONString: /"([^"\\\x00-\x1f]|\\(["\/\\bfnrt]|u{hex}{4}))*"/

fraction = /\.[0-9]+/
exp = /[eE][+-]?[0-9]+/
JSONNumber: /-?(0|[1-9][0-9]*){fraction}?{exp}?/

'null': /null/
'true': /true/
'false': /false/


:: parser

%input JSONText;

JSONText :
	  JSONValue ;

JSONValue :
	  'null'
	| 'true'
	| 'false'
	| JSONObject
	| JSONArray
	| JSONString
	| JSONNumber
;

JSONObject :
	  '{' JSONMemberList? '}' ;

JSONMember :
	  JSONString ':' JSONValue ;

JSONMemberList :
	  JSONMember
	| JSONMemberList ',' JSONMember
;

JSONArray :
	  '[' JSONElementList? ']' ;

JSONElementList :
	  JSONValue
	| JSONElementList ',' JSONValue
;
