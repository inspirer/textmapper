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
maxtoken = 2048
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCleanup = true
genCopyright = true

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

code:	/\{/			{ skipAction(); lapg_n.endoffset = getOffset(); }

# Grammar

%input input, expression;

input (AstRoot) ::=
	  options? lexer_parts grammar_partsopt              {  $$ = new AstRoot($options, $lexer_parts, $grammar_partsopt, source, ${left().offset}, ${left().endoffset}); }
;

options (List<AstOptionPart>) ::=
	  option											{ $$ = new ArrayList<AstOptionPart>(16); ${left()}.add($option); }
	| list=options option								{ $list.add($option); }
;

option (AstOptionPart) ::=
	  ID '=' expression 								{ $$ = new AstOption($ID, $expression, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem
;

identifier (AstIdentifier) ::=
	  ID												{ $$ = new AstIdentifier($ID, source, ${left().offset}, ${left().endoffset}); }
;

symref (AstReference) ::=
	  ID												{ $$ = new AstReference($ID, AstReference.DEFAULT, source, ${left().offset}, ${left().endoffset}); }
;

type (String) ::=
	  '(' scon ')'										{ $$ = $scon; }
	| '(' type_part_list ')'							{ $$ = source.getText(${first().offset}+1, ${last().endoffset}-1); }
;

type_part_list ::=
	  type_part_list type_part | type_part ;

type_part ::=
	  '<' | '>' | '[' | ']' | ID | '*' | '.' | ',' | '?' | '@' | '&' | '(' type_part_listopt ')' ;

pattern (AstRegexp) ::=
	  regexp											{ $$ = new AstRegexp($regexp, source, ${left().offset}, ${left().endoffset}); }
;

lexer_parts (List<AstLexerPart>) ::=
	  lexer_part 										{ $$ = new ArrayList<AstLexerPart>(64); ${left()}.add($lexer_part); }
	| list=lexer_parts lexer_part						{ $list.add($lexer_part); }
	| list=lexer_parts syntax_problem					{ $list.add($syntax_problem); }
;

lexer_part (AstLexerPart) ::=
	  state_selector
	| named_pattern
	| lexeme
;

named_pattern ::=
	  ID '=' pattern									{ $$ = new AstNamedPattern($ID, $pattern, source, ${left().offset}, ${left().endoffset}); }
;

lexeme ::=
	  identifier typeopt ':' (pattern lexem_transitionopt iconopt lexem_attrsopt commandopt)?
                                                    	{ $$ = new AstLexeme($identifier, $typeopt, $pattern, $lexem_transitionopt, $iconopt, $lexem_attrsopt, $commandopt, source, ${left().offset}, ${left().endoffset}); }
;

lexem_transition (AstReference) ::=
	  '=>' stateref										{ $$ = $1; }
;

lexem_attrs (AstLexemAttrs) ::=
	  '(' lexem_attribute ')'							{ $$ = $1; }
;

lexem_attribute (AstLexemAttrs) ::=
	  Lsoft												{ $$ = new AstLexemAttrs(org.textmapper.lapg.api.@LexerRule.KIND_SOFT, source, ${left().offset}, ${left().endoffset}); }
	| Lclass											{ $$ = new AstLexemAttrs(org.textmapper.lapg.api.@LexerRule.KIND_CLASS, source, ${left().offset}, ${left().endoffset}); }
	| Lspace											{ $$ = new AstLexemAttrs(org.textmapper.lapg.api.@LexerRule.KIND_SPACE, source, ${left().offset}, ${left().endoffset}); }
	| Llayout											{ $$ = new AstLexemAttrs(org.textmapper.lapg.api.@LexerRule.KIND_LAYOUT, source, ${left().offset}, ${left().endoffset}); }
;

state_selector ::=
	  '[' state_list ']'								{ $$ = new AstStateSelector($state_list, source, ${left().offset}, ${left().endoffset}); }
;

state_list (List<AstLexerState>) ::=
	  lexer_state										{ $$ = new ArrayList<Integer>(4); ${left()}.add($lexer_state); }
	| list=state_list ',' lexer_state					{ $list.add($lexer_state); }
;

stateref (AstReference) ::=
	  ID                                                { $$ = new AstReference($ID, AstReference.STATE, source, ${left().offset}, ${left().endoffset}); }
;

lexer_state (AstLexerState) ::=
	  identifier ('=>' defaultTransition=stateref)?		{ $$ = new AstLexerState($identifier, $defaultTransition, source, ${left().offset}, ${left().endoffset}); }
;

grammar_parts (List<AstGrammarPart>) ::=
	  grammar_part 										{ $$ = new ArrayList<AstGrammarPart>(64); ${left()}.add($grammar_part); }
	| list=grammar_parts grammar_part					{ $list.add($grammar_part); }
	| list=grammar_parts syntax_problem					{ $list.add($syntax_problem); }
;

grammar_part (AstGrammarPart) ::=
	  nonterm
	| directive
;

nonterm ::=
	  annotations? identifier nonterm_ast? typeopt Linline? '::=' rules ';'
	  													{ $$ = new AstNonTerm($identifier, $typeopt, $rules, $annotations, source, ${left().offset}, ${left().endoffset}); }
;

nonterm_ast ::=
	  Lextends references_cs
	| Lreturns symref
;

priority_kw (String) ::=
	Lleft | Lright | Lnonassoc ;

directive ::=
	  '%' priority_kw references ';'					{ $$ = new AstDirective($priority_kw, $references, source, ${left().offset}, ${left().endoffset}); }
	| '%' Linput inputs ';'								{ $$ = new AstInputDirective($inputs, source, ${left().offset}, ${left().endoffset}); }
;

inputs (List<AstInputRef>) ::=
	  inputref											{ $$ = new ArrayList<AstInputRef>(); ${left()}.add($inputref); }
	| list=inputs ',' inputref               			{ $list.add($inputref); }
;

inputref (AstInputRef) ::=
	symref Lnoeoiopt									{ $$ = new AstInputRef($symref, $Lnoeoiopt != null, source, ${left().offset}, ${left().endoffset}); }
;

references (List<AstReference>) ::=
	  symref											{ $$ = new ArrayList<AstReference>(); ${left()}.add($symref); }
	| list=references symref							{ $list.add($symref); }
;

references_cs (List<AstReference>) ::=
	  symref											{ $$ = new ArrayList<AstReference>(); ${left()}.add($symref); }
	| list=references_cs ',' symref						{ $list.add($symref); }
;

rules (List<AstRule>) ::=
 	rule_list
;

rule_list (List<AstRule>) ::=
	  rule0												{ $$ = new ArrayList<AstRule>(); ${left()}.add($rule0); }
	| list=rule_list '|' rule0							{ $list.add($rule0); }
;

rule0 (AstRule) ::=
	  ruleprefix? rhsParts? rule_attrsopt				{ $$ = new AstRule($ruleprefix, $rhsParts, $rule_attrsopt, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem									{ $$ = new AstRule($syntax_problem); }
;

ruleprefix (AstRulePrefix) ::=
	  annotations ':'									{ $$ = new AstRulePrefix($annotations, null); }
	| rhsAnnotations? alias=ID (Lextends references_cs)? ':'
														{ $$ = new AstRulePrefix($rhsAnnotations, $alias); }
;

rule_attrs (AstRuleAttribute) ::=
	'%' Lprio symref									{ $$ = new AstPrioClause($symref, source, ${left().offset}, ${left().endoffset}); }
	| '%' Lshift										{ $$ = new AstShiftClause(source, ${left().offset}, ${left().endoffset}); }
;

rhsParts (List<AstRulePart>) ::=
	  rhsPart											{ $$ = new ArrayList<AstRulePart>(); ${left()}.add($rhsPart); }
	| list=rhsParts rhsPart 							{ $list.add($rhsPart); }
	| list=rhsParts syntax_problem						{ $list.add($syntax_problem); }
;

%left '&';

rhsPart (AstRulePart) ::=
	  rhsAnnotations? (ID '=')? rhsPrimary				{ $$ = new AstRefRulePart($ID, $rhsPrimary, $rhsAnnotations, source, ${left().offset}, ${left().endoffset}); }
	| command
	| rhsUnordered
;

rhsUnordered ::=
	  left=rhsPart '&' right=rhsPart					{ $$ = new AstUnorderedRulePart($left, $right, source, ${left().offset}, ${left().endoffset}); }
;

rhsPrimary (AstRuleSymbolRef) ::=
	  rhsSymbol (Las symref)?                           { /* TODO handle as */ }
	| '(' rhsParts Lseparator references ')' '+'		{ $$ = new AstRuleNestedListWithSeparator($rhsParts, $references, true, source, ${left().offset}, ${left().endoffset}); }
	| '(' rhsParts Lseparator references ')' '*'		{ $$ = new AstRuleNestedListWithSeparator($rhsParts, $references, false, source, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '?'									{ $$ = new AstRuleNestedQuantifier($rhsPrimary, AstRuleNestedQuantifier.KIND_OPTIONAL, source, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '*'									{ $$ = new AstRuleNestedQuantifier($rhsPrimary, AstRuleNestedQuantifier.KIND_ZEROORMORE, source, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '+'									{ $$ = new AstRuleNestedQuantifier($rhsPrimary, AstRuleNestedQuantifier.KIND_ONEORMORE, source, ${left().offset}, ${left().endoffset}); }
;

# TODO AstRulePart -> TmaRhsPart
# TODO AstRefRulePart -> TmaRhsAssignment
# TODO add TmaRhsCast
# TODO AstRuleNestedNonTerm -> TmaRhsInner
# TODO AstRuleDefaultSymbolRef -> TmaRhsSymbol
# TODO AstRuleNestedListWithSeparator -> TmaRhsList
# TODO AstRuleNestedQuantifier -> TmaRhsQuantifier
# TODO AstUnorderedRulePart -> TmaRhsUnordered


rhsSymbol (AstRuleSymbolRef) ::=
	  symref											{ $$ = new AstRuleDefaultSymbolRef($symref, source, ${left().offset}, ${left().endoffset}); }
	| '(' rules ')'										{ $$ = new AstRuleNestedNonTerm($rules, source, ${left().offset}, ${left().endoffset}); }
;

rhsAnnotations (AstRuleAnnotations) ::=
	  annotation_list									{ $$ = new AstRuleAnnotations(null, $annotation_list, source, ${left().offset}, ${left().endoffset}); }
	| negative_la annotation_list						{ $$ = new AstRuleAnnotations($negative_la, $annotation_list, source, ${left().offset}, ${left().endoffset}); }
	| negative_la										{ $$ = new AstRuleAnnotations($negative_la, null, source, ${left().offset}, ${left().endoffset}); }
;

annotations (AstAnnotations) ::=
	annotation_list										{ $$ = new AstAnnotations($annotation_list, source, ${left().offset}, ${left().endoffset}); }
;

annotation_list (java.util.@List<AstNamedEntry>) ::=
	  annotation										{ $$ = new java.util.@ArrayList<AstNamedEntry>(); ${left()}.add($annotation); }
	| annotation_list annotation						{ $annotation_list.add($annotation); }
;

annotation (AstNamedEntry) ::=
	  '@' ID ('=' expression)?                          { $$ = new AstNamedEntry($ID, $expression, source, ${left().offset}, ${left().endoffset}); }
	| '@' syntax_problem                                { $$ = new AstNamedEntry($syntax_problem); }
;

negative_la (AstNegativeLA) ::=
	'(?!' negative_la_clause ')'						{ $$ = new AstNegativeLA($negative_la_clause, source, ${left().offset}, ${left().endoffset}); }
;

negative_la_clause (java.util.@List<AstReference>) ::=
	  symref											{ $$ = new java.util.@ArrayList<AstReference>(); ${left()}.add($symref); }
	| negative_la_clause '|' symref						{ $negative_la_clause.add($symref); }
;

##### EXPRESSIONS

expression (AstExpression) ::=
	  scon                                              { $$ = new AstLiteralExpression($scon, source, ${left().offset}, ${left().endoffset}); }
	| icon                                              { $$ = new AstLiteralExpression($icon, source, ${left().offset}, ${left().endoffset}); }
	| Ltrue                                             { $$ = new AstLiteralExpression(Boolean.TRUE, source, ${left().offset}, ${left().endoffset}); }
	| Lfalse                                            { $$ = new AstLiteralExpression(Boolean.FALSE, source, ${left().offset}, ${left().endoffset}); }
	| symref
	| Lnew name '(' map_entriesopt ')'					{ $$ = new AstInstance($name, $map_entriesopt, source, ${left().offset}, ${left().endoffset}); }
	| '[' expression_listopt ']'						{ $$ = new AstArray($expression_listopt, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem
;

expression_list (List<AstExpression>) ::=
	expression											{ $$ = new ArrayList(); ${left()}.add($expression); }
	| expression_list ',' expression					{ $expression_list.add($expression); }
;

map_entries (java.util.@List<AstNamedEntry>) ::=
	  ID map_separator expression						{ $$ = new java.util.@ArrayList<AstNamedEntry>(); ${left()}.add(new AstNamedEntry($ID, $expression, source, ${left().offset}, ${left().endoffset})); }
	| map_entries ',' ID map_separator expression		{ $map_entries.add(new AstNamedEntry($ID, $expression, source, ${ID.offset}, ${left().endoffset})); }
;

map_separator ::=
	':' | '=' | '=>' ;

name (AstName) ::=
	qualified_id 										{ $$ = new AstName($qualified_id, source, ${left().offset}, ${left().endoffset}); }
;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID								{ $$ = $qualified_id + "." + $ID; }
;

command (AstCode) ::=
	code												{ $$ = new AstCode(source, ${first().offset}+1, ${last().endoffset}-1); }
;

syntax_problem (AstError) ::=
	error												{ $$ = new AstError(source, ${left().offset}, ${left().endoffset}); }
;

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