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

lang = "java"
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

# Vocabulary

error:

ID(String): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)
			{ $symbol = current(); }

regexp(String):	/\/([^\/\\\n]|\\.)*\//	{ $symbol = token.toString().substring(1, token.length()-1); }
scon(String):	/"([^\n\\"]|\\.)*"/		{ $symbol = unescape(current(), 1, token.length()-1); }
icon(Integer):	/-?[0-9]+/				{ $symbol = Integer.parseInt(current()); }

eoi:           /%%.*(\r?\n)?/			{ templatesStart = lapg_n.endoffset; }
_skip:         /[\n\r\t ]+/		(space)
_skip_comment:  /#.*(\r?\n)?/			{ spaceToken = skipComments; }

'%':	/%/
'::=':  /::=/
'|':    /\|/
'=':	/=/
'=>':	/=>/
';':    /;/
'.':    /\./
',':	/,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':	/\(/
'(?!':	/\(\?!/
# TODO overlaps with ID '->':	/->/
')':	/\)/
'<':	/</
'>':	/>/
'*':	/*/
'+':	/+/
'+=':	/+=/
'?':	/?/
'&':	/&/
'@':	/@/

Ltrue:  /true/
Lfalse: /false/
Lnew:   /new/
Lseparator: /separator/
Las: /as/
Lextends: /extends/
Linline: /inline/

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
Lspace: /space/				(soft)
Llayout: /layout/			(soft)

# reserved

Lreduce: /reduce/

code:	/\{/							{ skipAction(); lapg_n.endoffset = getOffset(); }

# Grammar

%input input, expression;

input ::=
	  option+? lexer_parts grammar_partsopt
;

option ::=
	  ID '=' expression
	| syntax_problem
;

@_class
identifier ::=
	  ID ;

@_class
symref ::=
	  ID ;

type (String) ::=
	  '(' scon ')'						{ $$ = $scon; }
	| '(' type_part_list ')'			{ $$ = source.getText(${first().offset}+1, ${last().endoffset}-1); }
;

@noast
type_part_list ::=
	  type_part_list type_part | type_part ;

@noast
type_part ::=
	  '<' | '>' | '[' | ']' | ID | '*' | '.' | ',' | '?' | '@' | '&' | '(' type_part_list? ')' ;

pattern ::=
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
	  ID '=' pattern ;

lexeme ::=
	  identifier typeopt ':'
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

@_class
stateref ::=
	  ID ;

lexer_state ::=
	  identifier ('=>' defaultTransition=stateref)?	;

grammar_parts ::=
	  grammar_part
	| grammar_parts grammar_part
	| grammar_parts syntax_problem
;

grammar_part ::=
	  nonterm | directive ;

nonterm ::=
	  annotations? identifier nonterm_ast? typeopt Linline? '::=' rules ';' ;

nonterm_ast ::=
	  Lextends references_cs
	| Lreturns symref
;

priority_kw ::=
	Lleft | Lright | Lnonassoc ;

directive ::=
	  prio: '%' priority_kw references ';'
	| input: '%' Linput (inputref separator ',')+ ';'
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
	  annotations=annotations ':'
	| annotations=rhsAnnotations as annotation_list? alias=identifier (Lextends _extends=references_cs)? ':'
;

rhsSuffix ::=
	'%' kind=Lprio symref
	| '%' kind=Lshift
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

# TODO get rid of interfaces
rhsAnnotated ::=
	  rhsAssignment
	| impl: rhsAnnotations rhsAssignment
;

rhsAssignment ::=
	  rhsOptional
	| impl: identifier '=' rhsOptional
	| impl: identifier addition='+=' rhsOptional
;

rhsOptional ::=
	  rhsCast
	| impl: rhsCast '?'
;

rhsCast ::=
	  rhsPrimary
	| impl: rhsPrimary Las symref
;

rhsUnordered ::=
	  left=rhsPart '&' right=rhsPart
;

# TODO +/-, separator, etc.
@_class
rhsPrimary ::=
	  symref
	| '(' rules ')'
	| '(' rhsParts Lseparator references ')' '+'
	| '(' rhsParts Lseparator references ')' '*'
	| rhsPrimary '*'
	| rhsPrimary '+'
;

rhsAnnotations ::=
	  annotation_list
	| negative_la annotation_list
	| negative_la
;

annotations ::=
	annotation_list
;

annotation_list ::=
	  annotation
	| annotation_list annotation
;

annotation ::=
	  '@' ID ('=' expression)?
	| '@' syntax_problem
;

negative_la ::=
	'(?!' unwantedSymbols=(symref separator '|')+ ')' ;


##### EXPRESSIONS

expression ::=
	  literal: sval=scon
	| literal: ival=icon
	| literal: isTrue=Ltrue       # TODO val=Ltrue/Boolean.TRUE?/
	| literal: isFalse=Lfalse	   # TODO val=Lfalse/Boolean.FALSE?/
	| symref
	| instance: Lnew name '(' map_entriesopt ')'
	| array: '[' (expression separator ',')* ']'
	| syntax_problem
;

map_entries ::=
	  ID map_separator expression
	| map_entries ',' ID map_separator expression
;

@noast
map_separator ::=
	':' | '=' | '=>' ;

@_class
name ::=
	qualified_id ;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID				{ $$ = $qualified_id + "." + $ID; }
;

@_class
command ::=
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