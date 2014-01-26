#  syntax: lalr1 generator source grammar

#  Copyright 2002-2013 Evgeny Gryaznov
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

language textmapper(java);

prefix = "TM"
package = "org.textmapper.tool.parser"
astprefix = "Tma"
maxtoken = 2048
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCleanup = true
genCopyright = true
genast = true
genastdef = true

:: lexer

[initial, afterAt => initial, afterAtID => initial]

regexp(String):	/\/([^\/\\\n]|\\.)*\//	{ $symbol = token.toString().substring(1, token.length()-1); }
scon(String):	/"([^\n\\"]|\\.)*"/		{ $symbol = unescape(current(), 1, token.length()-1); }
icon(Integer):	/-?[0-9]+/				{ $symbol = Integer.parseInt(current()); }

eoi:           /%%.*(\r?\n)?/			{ templatesStart = lapg_n.endoffset; }
_skip:         /[\n\r\t ]+/		(space)
_skip_comment:  /#.*(\r?\n)?/			{ spaceToken = skipComments; }

'%':	/%/
'::=':  /::=/
'::':   /::/
'|':    /\|/
'=':	/=/
'=>':	/=>/
';':    /;/
'.':    /\./
'..':   /\.\./
',':	/,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':	/\(/
'(?!':	/\(\?!/
# TODO overlaps with ID '->':	/->/
')':	/\)/
'}':	/\}/
'<':	/</
'>':	/>/
'*':	/*/
'+':	/+/
'+=':	/+=/
'?':	/?/
'&':	/&/
'$':	/$/
'@':    /@/ => afterAt

error:

[initial, afterAt => afterAtID, afterAtID => initial]

ID(String): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)    { $symbol = current(); }

Ltrue:  /true/
Lfalse: /false/
Lnew:   /new/
Lseparator: /separator/
Las: /as/
Limport: /import/
Linline: /inline/			(soft)

Lprio:  /prio/				(soft)
Lshift: /shift/				(soft)

Lreturns: /returns/			(soft)

Linput: /input/				(soft)
Lleft:  /left/				(soft)
Lright: /right/				(soft)
Lnonassoc: /nonassoc/		(soft)

Lnoeoi: /no-eoi/			(soft)

Lsoft: /soft/				(soft)
Lclass: /class/				(soft)
Linterface: /interface/		(soft)
Lvoid: /void/				(soft)
Lspace: /space/				(soft)
Llayout: /layout/			(soft)
Llanguage: /language/       (soft)
Llalr: /lalr/				(soft)

Llexer: /lexer/				(soft)
Lparser: /parser/			(soft)

# reserved

Lreduce: /reduce/

[initial, afterAt => initial]

code:    /\{/                            { skipAction(); lapg_n.endoffset = getOffset(); }

[afterAtID => initial]
'{':	/\{/


:: parser

%input input, expression;

input ::=
	  header import_* option* lexer_section parser_section? ;

header ::=
	  Llanguage name ('(' target=name ')')? parsing_algorithmopt ';' ;

lexer_section ::=
	  '::' Llexer lexer_parts ;

parser_section ::=
	  '::' Lparser grammar_parts ;

parsing_algorithm ::=
	  Llalr '(' la=icon ')' ;

import_ ::=
	  Limport alias=ID? file=scon ';' ;

option ::=
	  ID '=' expression
	| syntax_problem
;

identifier class ::=
	  ID ;

symref class ::=
	  name=ID ;

type (String) ::=
	  '(' scon ')'						{ $$ = $scon; }
	| '(' type_part_list ')'			{ $$ = source.getText(${first().offset}+1, ${last().endoffset}-1); }
;

type_part_list void ::=
	  type_part_list type_part | type_part ;

type_part void ::=
	  '<' | '>' | '[' | ']' | ID | '*' | '.' | ',' | '?' | '@' | '&' | '(' type_part_list? ')' ;

pattern class ::=
	  regexp
;

lexer_parts ::=
	  lexer_part
	| lexer_parts lexer_part
	| lexer_parts syntax_problem
;

lexer_part ::=
	  state_selector
	| named_pattern
	| lexeme
;

named_pattern ::=
	  name=ID '=' pattern ;

lexeme ::=
	  name=identifier typeopt ':'
			(pattern lexem_transitionopt priority=iconopt lexem_attrsopt commandopt)? ;

lexem_transition ::=
	  '=>' @pass stateref ;

lexem_attrs ::=
	  '(' lexem_attribute ')' ;

lexem_attribute ::=
	  Lsoft
	| Lclass
	| Lspace
	| Llayout
;

state_selector ::=
	  '[' states=(lexer_state separator ',')+ ']' ;

stateref class ::=
	  name=ID ;

lexer_state ::=
	  name=identifier ('=>' defaultTransition=stateref)?	;

grammar_parts ::=
	  grammar_part
	| grammar_parts grammar_part
	| grammar_parts syntax_problem
;

grammar_part ::=
	  nonterm | directive ;

nonterm ::=
	  annotations? name=identifier type=nonterm_type? '::=' rules ';' ;

nonterm_type interface ::=
	  [nontermTypeAST] Lreturns symref
	| [nontermTypeHint] isInline=Linline? kind=Lclass name=identifieropt
	| [nontermTypeHint] kind=Linterface name=identifieropt
	| [nontermTypeHint] kind=Lvoid
	| [nontermTypeRaw] type
;

assoc ::=
	Lleft | Lright | Lnonassoc ;

directive returns grammar_part ::=
	  [directivePrio] '%' assoc symbols=references ';'
	| [directiveInput] '%' Linput inputRefs=(inputref separator ',')+ ';'
;

inputref ::=
	symref noeoi=Lnoeoi? ;

references ::=
	  symref
	| references symref
;

references_cs ::=
	  symref
	| references_cs ',' symref
;

rules ::=
 	(rule0 separator '|')+ ;

rule0 ::=
	  rhsPrefix? rhsParts? rhsSuffixopt
	| syntax_problem
;

rhsPrefix ::=
	  '[' annotations=annotations ']'
	| '[' annotations=annotations? alias=identifier ']'
;

rhsSuffix ::=
	  '%' kind=Lprio symref
	| '%' kind=Lshift symref
;

rhsParts ::=
	  rhsPart
	| rhsParts rhsPart
	| rhsParts syntax_problem
;

%left '&';

rhsPart ::=
	  rhsAnnotated
	| rhsUnordered
	| command
;

rhsAnnotated returns rhsPart ::=
	  rhsAssignment
	| rhsAnnotations rhsAssignment
;

rhsAssignment returns rhsPart ::=
	  rhsOptional
	| id=identifier ('=' | addition='+=') inner=rhsOptional
;

rhsOptional returns rhsPart ::=
	  rhsCast
	| [rhsQuantifier] inner=rhsCast quantifier='?'
;

rhsCast returns rhsPart ::=
	  rhsClass
	| rhsClass Las symref
	| [rhsAsLiteral] rhsClass Las literal
;

rhsUnordered returns rhsPart ::=
	  left=rhsPart '&' right=rhsPart
;

rhsClass returns rhsPart ::=
	  rhsPrimary
	| identifier ':' inner=rhsPrimary
;

rhsPrimary returns rhsPart ::=
	  [rhsSymbol] symref
	| [rhsNested] '(' rules ')'
	| [rhsList] '(' rhsParts Lseparator references ')' quantifier='+'
	| [rhsList] '(' rhsParts Lseparator references ')' quantifier='*'
	| [rhsQuantifier] inner=rhsPrimary quantifier='*'
	| [rhsQuantifier] inner=rhsPrimary quantifier='+'
	| [rhsIgnored] '$' '(' rules (';' brackets=(rhsBracketsPair separator ',')+)? ')'
;

rhsBracketsPair ::=
	  lhs=symref '..' rhs=symref
;

rhsAnnotations ::=
	  annotation+
	| negative_la annotation+
	| negative_la
;


annotations class ::=
	annotations=annotation+ ;

annotation ::=
	  '@' name=ID ('{' expression '}')?
	| '@' syntax_problem
;

negative_la ::=
	'(?!' unwantedSymbols=(symref separator '|')+ ')' ;


##### EXPRESSIONS

expression ::=
	  literal
	| symref
	| [instance] Lnew className=name '(' map_entriesopt ')'
	| [array] '[' content=(expression separator ',')* ']'
	| syntax_problem
;

literal ::=
	  [literal] sval=scon
	| [literal] ival=icon
	| [literal] val=Ltrue as true
    | [literal] val=Lfalse as false

;

map_entries ::=
	  ID map_separator expression
	| map_entries ',' ID map_separator expression
;

map_separator void ::=
	':' | '=' | '=>' ;

name class ::=
	qualified_id ;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID				{ $$ = $qualified_id + "." + $ID; }
;

command class ::=
	code ;

syntax_problem ::=
	error ;

##################################################################################

%%

${template java.imports-}
${call base-}
import java.util.List;
import java.util.ArrayList;
import org.textmapper.tool.parser.ast.*;
${end}

${template java_lexer.lexercode}
private int deep = 0;
private int templatesStart = -1;
private boolean skipComments = true;

int getTemplatesStart() {
	return templatesStart;
}

public void setSkipComments(boolean skip) {
	this.skipComments = skip;
}

private boolean skipAction() throws java.io.@IOException {
	final int[] ind = new int[] { 0 };
	org.textmapper.tool.parser.action.@SActionLexer.ErrorReporter innerreporter = new org.textmapper.tool.parser.action.@SActionLexer.ErrorReporter() {
		public void error(int start, int line, String s) {
			reporter.error(start, start + 1, line, s);
		}
	};
	org.textmapper.tool.parser.action.@SActionLexer l = new org.textmapper.tool.parser.action.@SActionLexer(innerreporter) {
		@Override
		protected char nextChar() throws java.io.@IOException {
			if (ind[0] < 2) {
				return ind[0]++ == 0 ? '{' : chr;
			}
			TMLexer.this.advance();
			return chr;
		}
	};
	org.textmapper.tool.parser.action.@SActionParser p = new org.textmapper.tool.parser.action.@SActionParser(innerreporter);
	try {
		p.parse(l);
	} catch (org.textmapper.tool.parser.action.@SActionParser.ParseException e) {
		reporter.error(getOffset(), getOffset() + 1, getLine(), "syntax error in action");
		return false;
	}
	return true;
}

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


${template java.classcode}
${call base-}
org.textmapper.tool.parser.TMTree.@TextSource source;
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}

${template java_tree.parseStatements-}
${call base-}
${if inp.target.id == 'input'-}
if (result != null) {
	result.setTemplatesStart(lexer.getTemplatesStart());
}
${end-}
${end}