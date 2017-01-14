#  syntax: template types definition grammar

#  Copyright 2002-2016 Evgeny Gryaznov
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

language types(java);

lang = "java" 
prefix = "Types"
package = "org.textmapper.templates.types"
gentree = true
genast = true
genastdef = true
astprefix = "Ast"
positions = "line,offset"
endpositions = "offset"
genCopyright = true

:: lexer

identifier {String}: /[a-zA-Z_][a-zA-Z_0-9]*|'([^\n\\']|\\.)*'/ -1   (class)
			{ $$ = tokenText(); }

scon {String}:	/"([^\n\\"]|\\.)*"/		{ $$ = unescape(tokenText(), 1, tokenSize()-1); }
icon {Integer}:	/-?[0-9]+/				{ $$ = Integer.parseInt(tokenText()); }
bcon {Boolean}:  /true|false/			{ $$ = tokenText().equals("true"); }

_skip:         /[\n\t\r ]+/	(space)
_skip:  /#.*/	(space)

'..':    /\.\./
'.':    /\./
'*':    /\*/
';':    /;/
',':	/,/
':':    /:/
'=':    /=/
'=>':	/=>/
'{':	/\{/
'}':	/\}/
'(':	/\(/
')':	/\)/
'[':	/\[/
']':	/\]/

Lclass:		 /class/
Lextends:	 /extends/

Lint:		 /int/
Lbool:		 /bool/
Lstring:	 /string/

Lset:		 /set/
Lchoice:	 /choice/


:: parser

input class ::=
	declarations ;

declarations ::=
	declarations type_declaration | type_declaration ;

type_declaration ::=
	Lclass name=identifier super=extends_clauseopt '{' members=member_declaration* '}'
;

extends_clause ::=
	Lextends @pass name_list ;

##### DECLARATIONS

member_declaration ::=
	feature_declaration
  | method_declaration
;

feature_declaration ::=
	type_ex name=identifier modifiersopt defaultvalopt ';' ;

method_declaration ::=
	returnType=type_ex name=identifier '(' parametersopt ')' ';' ;

parameters ::=
	type_ex | parameters ',' type_ex ;

defaultval ::=
	'=' @pass expression ;

modifiers ::=
	'[' @pass (constraint separator ';')+ ']' ;

constraint ::=
	string_constraint | (multiplicity separator ',')+ ;

string_constraint ::=
	kind=Lset ':' strings
  | kind=Lchoice ':' strings
  | identifier
;

strings ::=
	strings ',' string | string ;
	
string ::=
	identifier | scon ;	

multiplicity ::=
	lo=icon
  | lo=icon '..' hasNoUpperBound='*'
  | lo=icon '..' hi=icon
;


##### TYPES

type_ex ::=
	type
  |	type '[' (multiplicity separator ',')+ ']'
;

type ::=
	kind=Lint
  | kind=Lstring
  | kind=Lbool
  | name
  | name isReference='*'
  | isClosure='{' parametersopt '=>' '}'
;

##### EXPRESSIONS

expression ::=
	structural_expression | literal_expression ;

literal_expression ::=
	  scon
	| icon
	| bcon
;

structural_expression ::=
	  name '(' mapEntries=(identifier map_separator expression separator ',')* ')'
	| '[' expression_listopt ']'
;

expression_list ::=
	expression
	| expression_list ',' expression
;

map_separator ::=
	':' | '=' | '=>' ;

name ::=
	  identifier
	| name '.' identifier
;

name_list ::=
	  name
	| name_list ',' name
;

##################################################################################

%%

${template java_lexer.lexercode}
private String unescape(String s, int start, int end) {
	StringBuilder sb = new StringBuilder();
	end = Math.min(end, s.length());
	for(int i = start; i < end; i++) {
		char c = s.charAt(i);
		if(c == '\\') {
			if(++i == end) {
				break;
			}
			c = s.charAt(i);
			if(c == 'u' || c == 'x') {
				// FIXME process unicode
			} else if(c == 'n') {
				sb.append('\n');
			} else if(c == 'r') {
				sb.append('\r');
			} else if(c == 't') {
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