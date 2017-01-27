language json(java);

prefix = "Json"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"
package = "org.textmapper.json"

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

%%

${template java_lexer.lexercode}
private String unescape(String s, int start, int end) {
	StringBuilder sb = new StringBuilder();
	end = Math.min(end, s.length());
	for (int i = start; i < end; i++) {
		char c = s.charAt(i);
		if (c == '\\') {
			if (++i == end) {
				break;
			}
			c = s.charAt(i);
			if (c == 'u' || c == 'x') {
				// FIXME process unicode
			} else if (c == 'n') {
				sb.append('\n');
			} else if (c == 'r') {
				sb.append('\r');
			} else if (c == 't') {
				sb.append('\t');
			} else {
				sb.append(c);
			}
		} else {
			sb.append(c);
		}
	}
	return sb.toString();
}
${end}
