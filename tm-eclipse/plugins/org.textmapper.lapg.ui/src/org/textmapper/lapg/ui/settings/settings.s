# lapg syntax file

lang = "java"
prefix = "Settings"
package = "org.textmapper.lapg.ui.settings"
gentree = true
genast = true
positions = "offset"
endpositions = "offset"
astsubpackage = ""

identifier(String): /[a-zA-Z_\-][a-zA-Z_\-0-9]*/ -1	{ $lexem = current(); break; }
scon(String):   /"([^\n\\"]|\\.)*"/				{ $lexem = unescape(current(), 1, token.length()-1); break; }
_skip:          /[\n\t\r ]+/					{ return false; }

'[':   /\[/
']':   /\]/
'(':   /\(/
')':   /\)/
'=':   /=/
',':   /,/

Ldef:  /def/

# grammar

input ::=
	settings_list ;

settings_list ::=
	settings
	| settings_list settings
; 

settings ::=
	'[' scon ']'
	options_list 
;

options_list ::=
	option
	| options_list option
;

option ::=
	identifier 
	| identifier '=' scon
	| identifier '=' '(' string_list ')'
	| isVardef=Ldef identifier '=' scon
;

string_list ::=
	scon
	| string_list ',' scon
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
