#  syntax: lalr1 generator source grammar

#  Copyright 2002-2015 Evgeny Gryaznov
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

reClass = /\[([^\n\r\]\\]|\\.)*\]/
reFirst = /[^\n\r\*\[\\\/]|\\.|{reClass}/
reChar = /{reFirst}|\*/

regexp(String): /\/{reFirst}{reChar}*\// { $$ = tokenText().substring(1, tokenSize()-1); }
scon(String):	/"([^\n\\"]|\\.)*"/		{ $$ = unescape(tokenText(), 1, tokenSize()-1); }
icon(Integer):	/-?[0-9]+/				{ $$ = Integer.parseInt(tokenText()); }

eoi:           /%%.*(\r?\n)?/			{ templatesStart = token.endoffset; }
_skip:         /[\n\r\t ]+/		(space)
_skip_comment:  /#.*(\r?\n)?/			{ spaceToken = skipComments; }

commentChars = /([^*]|\*+[^*\/])*\**/
_skip_multiline: /\/\*{commentChars}\*\// (space)


'%':	/%/
'::=':  /::=/
'::':   /::/
'|':    /\|/
'||':    /\|\|/
'=':	/=/
'==':   /==/
'!=':   /!=/
'=>':	/=>/
';':    /;/
'.':    /\./
',':	/,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':	/\(/
# TODO overlaps with ID '->':	/->/
')':	/\)/
'{~':	/\{~/
'}':	/\}/
'<':	/</
'>':	/>/
'*':	/\*/
'+':	/+/
'+=':	/+=/
'?':	/?/
'!':	/!/
'~':	/~/
'&':	/&/
'&&':	/&&/
'$':	/$/
'@':    /@/ => afterAt

error:

[initial, afterAt => afterAtID, afterAtID => initial]

ID(String): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)    { $$ = tokenText(); }

Ltrue:  /true/
Lfalse: /false/
Lnew:   /new/
Lseparator: /separator/
Las: /as/
Limport: /import/
Lset: /set/

Lbrackets: /brackets/		(soft)

Linline: /inline/			(soft)

Lprec:  /prec/				(soft)
Lshift: /shift/				(soft)

Lreturns: /returns/			(soft)

Linput: /input/				(soft)
Lleft:  /left/				(soft)
Lright: /right/				(soft)
Lnonassoc: /nonassoc/		(soft)

Lgenerate: /generate/		(soft)
Lassert: /assert/			(soft)
Lempty: /empty/				(soft)
Lnonempty: /nonempty/		(soft)

Lglobal: /global/		    (soft)
Lexplicit: /explicit/		(soft)
Llookahead: /lookahead/		(soft)
Lparam: /param/			    (soft)
Lflag: /flag/				(soft)

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

code:   /\{/     { skipAction(); token.endoffset = getOffset(); }

[afterAtID => initial]
'{':	/\{/


:: parser

%input input, expression;

input ::=
	  header imports=import_* options=option* lexer=lexer_section parser=parser_section? ;

header ::=
	  Llanguage name ('(' target=name ')')? parsing_algorithmopt ';' ;

lexer_section ::=
	  '::' Llexer @pass lexer_parts ;

parser_section ::=
	  '::' Lparser @pass grammar_parts ;

parsing_algorithm ::=
	  Llalr '(' la=icon ')' ;

import_ ::=
	  Limport alias=ID? file=scon ';' ;

option ::=
	  key=ID '=' value=expression
	| syntax_problem
;

identifier class ::=
	  ID ;

symref class ::=
	  name=ID args=symref_args? ;

symref_noargs returns symref ::=
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
	| lexer_directive
;

named_pattern ::=
	  name=ID '=' pattern ;

lexeme ::=
	  name=identifier typeopt ':'
			(pattern transition=lexeme_transitionopt priority=iconopt attrs=lexeme_attrsopt commandopt)? ;

lexeme_transition ::=
	  '=>' @pass stateref ;

lexeme_attrs ::=
	  '(' kind=lexeme_attribute ')' ;

lexeme_attribute ::=
	  Lsoft
	| Lclass
	| Lspace
	| Llayout
;

lexer_directive returns lexer_part ::=
	  '%' Lbrackets opening=symref_noargs closing=symref_noargs ';'		{~directiveBrackets}
;

state_selector ::=
	  '[' states=(lexer_state separator ',')+ ']' ;

stateref class ::=
	  name=ID ;

lexer_state ::=
	  name=identifier ('=>' defaultTransition=stateref)? ;

grammar_parts ::=
	  grammar_part
	| grammar_parts grammar_part
	| grammar_parts syntax_problem
;

grammar_part ::=
	  nonterm | template_param | directive ;

nonterm ::=
	  annotations? name=identifier params=nonterm_params? type=nonterm_type? '::=' rules ';' ;

nonterm_type interface ::=
	  Lreturns reference=symref_noargs									{~nontermTypeAST}
	| inline=Linline? kind=Lclass name=identifieropt implementsopt		{~nontermTypeHint}
	| kind=Linterface name=identifieropt implementsopt					{~nontermTypeHint}
	| kind=Lvoid														{~nontermTypeHint}
	| typeText=type														{~nontermTypeRaw}
;

implements ::=
	  ':' @pass references_cs ;

assoc ::=
	  Lleft | Lright | Lnonassoc ;

param_modifier ::=
	  Lexplicit
	| Lglobal
	| Llookahead
;

template_param returns grammar_part ::=
	  '%' modifier=param_modifier? param_type name=identifier ('=' param_value)? ';'
;

directive returns grammar_part ::=
	  '%' assoc symbols=references ';' 						        {~directivePrio}
	| '%' Linput inputRefs=(inputref separator ',')+ ';'	        {~directiveInput}
	| '%' Lassert (kind=Lempty | kind=Lnonempty) rhsSet ';'			{~directiveAssert}
	| '%' Lgenerate name=ID '=' rhsSet ';'							{~directiveSet}
;

inputref ::=
	  reference=symref_noargs noeoi=Lnoeoi? ;

references ::=
	  symref_noargs
	| references symref_noargs
;

references_cs ::=
	  symref_noargs
	| references_cs ',' symref_noargs
;

rules ::=
	  (rule0 separator '|')+ ;

rule0 ::=
	  predicate? prefix=rhsPrefix? list=rhsParts? action=ruleAction? suffix=rhsSuffixopt
	| error=syntax_problem
;

predicate ::=
	  '[' @pass predicate_expression ']' ;

rhsPrefix ::=
	  annotations ':'
;

rhsSuffix ::=
	  '%' kind=Lprec symref=symref_noargs
	| '%' kind=Lshift symref=symref_noargs
;

ruleAction ::=
	  '{~' action=identifier parameter=scon? '}' ;

rhsParts ::=
	  rhsPart
	| rhsParts rhsPart
	| rhsParts syntax_problem
;

%left '|';
%left '&';

rhsPart ::=
	  rhsAnnotated
	| rhsUnordered
	| command
	| rhsStateMarker
;

rhsStateMarker ::=
	  '.' name=ID ;

rhsAnnotated returns rhsPart ::=
	  rhsAssignment
	| annotations inner=rhsAssignment
;

rhsAssignment returns rhsPart ::=
	  rhsOptional
	| id=identifier ('=' | addition='+=') inner=rhsOptional
;

rhsOptional returns rhsPart ::=
	  rhsCast
	| inner=rhsCast quantifier='?' 		{~rhsQuantifier}
;

rhsCast returns rhsPart ::=
	  rhsClass
	| inner=rhsClass Las target=symref
	| inner=rhsClass Las literal 		{~rhsAsLiteral}
;

rhsUnordered returns rhsPart ::=
	  left=rhsPart '&' right=rhsPart
;

rhsClass returns rhsPart ::=
	  rhsPrimary
	| identifier ':' inner=rhsPrimary
;

rhsPrimary returns rhsPart ::=
	  reference=symref 																		{~rhsSymbol}
	| '(' rules ')' 																		{~rhsNested}
	| '(' ruleParts=rhsParts Lseparator separator_=references ')' atLeastOne='+' as true 	{~rhsList}
	| '(' ruleParts=rhsParts Lseparator separator_=references ')' atLeastOne='*' as false 	{~rhsList}
	| inner=rhsPrimary quantifier='*'														{~rhsQuantifier}
	| inner=rhsPrimary quantifier='+'														{~rhsQuantifier}
	| '$' '(' rules ')' 					{~rhsIgnored}
	| rhsSet
;

rhsSet returns rhsPart ::=
	  Lset '(' expr=setExpression ')'
;

setPrimary returns setExpression ::=
	  operator=ID? symbol=symref 			{~setSymbol}
	| '(' inner=setExpression ')' 			{~setCompound}
	| '~' inner=setPrimary 					{~setComplement}
;

setExpression interface ::=
	  setPrimary
	| left=setExpression kind='|' right=setExpression 	{~setBinary}
	| left=setExpression kind='&' right=setExpression 	{~setBinary}
;

annotations class ::=
	  annotations=annotation+ ;

annotation ::=
	  '@' name=ID ('{' expression '}')?
	| '@' syntax_problem
;

##### Nonterminal parameters

nonterm_params ::=
	  '<' list=(nonterm_param separator ',')+ '>' ;

nonterm_param interface ::=
	  param_ref
	| param_type=ID name=identifier ('=' param_value)?     {~inlineParameter}
;

param_ref ::=
	  ref=identifier ;

symref_args ::=
	  '<' arg_list=(argument separator ',')* '>' ;

argument ::=
	  name=param_ref ':' val=param_value
	| (bool='+'|bool='~')? name=param_ref
;

param_type ::=
	  Lflag | Lparam ;

param_value ::=
	  literal
	| symref_noargs
;

predicate_primary returns predicate_expression ::=
	  negated='!'? param_ref {~boolPredicate}
	| param_ref (kind='==' | kind='!=') literal {~comparePredicate}
;

%left '||';
%left '&&';

predicate_expression interface ::=
	  predicate_primary
	| left=predicate_expression kind='&&' right=predicate_expression {~predicateBinary}
	| left=predicate_expression kind='||' right=predicate_expression {~predicateBinary}
;

##### EXPRESSIONS

# TODO use json, get rid of new & symref

expression ::=
	  literal
	| symref
	| Lnew className=name '(' entries=(map_entry separator ',')* ')' 	{~instance}
	| '[' content=(expression separator ',')* ']' 	{~array}
	| syntax_problem
;

map_entry ::=
	  name=ID ':' value=expression ;

literal ::=
	  value=scon
	| value=icon
	| value=Ltrue as true
	| value=Lfalse as false
;

name class ::=
	  qualified_id ;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID				{ $$ = $qualified_id + "." + $ID; }
;

command class ::=
	  code
;

syntax_problem class : lexer_part, grammar_part, rhsPart ::=
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
	org.textmapper.tool.parser.action.@SActionLexer.ErrorReporter innerreporter = (String message, int line, int offset) ->
			reporter.error(message, line, offset, offset + 1);
	org.textmapper.tool.parser.action.@SActionLexer l = new org.textmapper.tool.parser.action.@SActionLexer(innerreporter) {
		@Override
		protected int nextChar() throws java.io.@IOException {
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
		reporter.error("syntax error in action", getLine(), getOffset(), getOffset() + 1);
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

${query java_ast.astSource() = 'source'}
${query java_ast.astNodeExtends() = ' extends org.textmapper.lapg.api.@TextSourceElement'}

${template java_ast.ast_class_fields(cl)-}
${if cl.name == 'Input'-}
	private int templatesStart = -1;
${end-}
${end}

${template java_ast.ast_class_methods(cl)-}
${if cl.name == 'Input'}
	public int getTemplatesStart() {
		return templatesStart;
	}

	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}
${end-}
${end}
