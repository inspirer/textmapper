#  syntax: generator options definition grammar
#
#  Lapg (Lexer and Parser Generator)
#  Copyright 2002-2010 Evgeny Gryaznov
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

.lang        "java" 
.prefix      "Optdef"
.package	 "net.sf.lapg.gen.options"
.lexemend    "on"
.gentree	 "on"
.genast		 "on"
.astprefix	 "AstOpt"
.positions   "line,offset"
.endpositions "offset"

# Vocabulary

[0]

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*|'([^\n\\']|\\.)*'/ -1
			{ $lexem = current(); break; }

scon(String):	/"([^\n\\"]|\\.)*"/		{ $lexem = unescape(current(), 1, token.length()-1); break; }
icon(Integer):	/-?[0-9]+/				{ $lexem = Integer.parseInt(current()); break; }

_skip:         /[\n\t\r ]+/    		{ return false; }
_skip:  /#.*/

';':    /;/
',':	/,/
':':    /:/
'=':    /=/
'{':	/\{/
'}':	/\}/
'(':	/\(/
')':	/\)/
'[':	/\[/
']':	/\]/

Lset:		 /set/
Lchoice:	 /choice/
Luint:		 /uint/
Lidentifier: /identifier/
Lqualified:	 /qualified/
Lbool:		 /bool/

Lglobal:	 /global/
Ltitle:		 /title/
Ldefault:	 /default/
Lnotempty:	 /notempty/
Ltypes:		 /types/
Lstring:	 /string/

Lsymbol:	 /symbol/
Lrule:		 /rule/
Lref:		 /ref/
Larray:		 /array/
Lstruct:	 /struct/

# Grammar

input ::=
	groups ;

groups ::=
	groups group | group ;

group ::=
	Lglobal title=scon '{' declarations '}'
  | kind=anno_kind '{' declarations '}'
  | Ltypes '{' typedefs '}'
;

anno_kind ::=
	Lsymbol | Lrule | Lref ;

##### DECLARATIONS

declarations ::=
	declarations declaration | declaration ;

declaration ::=
	identifier ':' type modifiersopt defaultval optionslistopt ;

optionslist ::=
	optionslist ',' option | ';' option ;

option ::=
	Ltitle scon ;

defaultval ::=
	Ldefault expression ;

modifiers ::=
	modifiers modifier | modifier ;

modifier ::=
	Lnotempty ;

##### TYPES

typedefs ::=
	typedefs typedef | typedef ;

typedef ::=
	identifier '=' type ';' ;

type ::=
	identifier
  | Luint
  | Lstring
  | Lidentifier
  | Lqualified
  | Lsymbol
  | Lbool
  | Lbool '(' trueVal = string ',' falseVal = string Commaopt ')'
  | Lset '(' strings ')'
  | Lchoice '(' strings ')'
  | Larray '(' type ')'
  | Lstruct '{' declarations '}'
;

Commaopt ::= ',' | ;

strings ::=
	strings ',' string | string ;
	
string ::=
	identifier | scon ;	

##### EXPRESSIONS

expression ::=
	structural_expression | literal_expression ;

literal_expression ::=
	  scon
	| icon
;

structural_expression ::=
	  '[' map_entries ']'
	| '[' expression_list ']'
;

expression_list ::=
	expression
	| expression_list ',' expression
;

map_entries ::=
	  identifier ':' expression
	| map_entries ',' identifier ':' expression
;

someA ::= map_entries | structural_expression ;

someB ::= structural_expressionopt ;

kind1 ::= ',' | ';' ;

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