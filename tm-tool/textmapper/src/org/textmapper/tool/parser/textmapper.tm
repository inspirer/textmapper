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
maxtoken = 2048
breaks = true
gentree = true
genast = true
positions = "line,offset"
endpositions = "offset"
genCleanup = true
genCopyright = true

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
'..':    /\.\./
',':	/,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':	/\(/
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

ID(String): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)
			{ $symbol = current(); }

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

code:	/\{/			{ skipAction(); lapg_n.endoffset = getOffset(); }

[afterAtID => initial]
'{':	/\{/


:: parser

%input input, expression;

input (TmaInput) ::=
	  header importsopt options? lexer_section parser_section?
	  													{ $$ = new TmaInput($header, $importsopt, $options, $lexer_section, $parser_section, source, ${left().offset}, ${left().endoffset}); }
;

header (TmaHeader) ::=
	  Llanguage name ('(' target=name ')')? parsing_algorithmopt ';'
														{ $$ = new TmaHeader($target, $name, $parsing_algorithmopt,  source, ${left().offset}, ${left().endoffset}); }
;

lexer_section (List<ITmaLexerPart>) ::=
	  '::' Llexer lexer_parts							{ $$ = $2; }
;

parser_section (List<ITmaGrammarPart>) ::=
	  '::' Lparser grammar_parts						{ $$ = $2; }
;

# ignored
parsing_algorithm (TmaParsingAlgorithm) ::=
	  Llalr '(' la=icon ')'								{ $$ = new TmaParsingAlgorithm($la, source, ${left().offset}, ${left().endoffset}); }
;

imports (List<TmaImport>) ::=
	  import_											{ $$ = new ArrayList<TmaImport>(16); ${left()}.add($import_); }
	| list=imports import_								{ $list.add($import_); }
;


import_ (TmaImport) ::=
	  Limport alias=ID? file=scon ';'					{ $$ = new TmaImport($alias, $file, source, ${left().offset}, ${left().endoffset}); }
;


options (List<TmaOption>) ::=
	  option											{ $$ = new ArrayList<TmaOption>(16); ${left()}.add($option); }
	| list=options option								{ $list.add($option); }
;

option (TmaOption) ::=
	  ID '=' expression 								{ $$ = new TmaOption($ID, $expression, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem									{ $$ = new TmaOption($syntax_problem, source, ${left().offset}, ${left().endoffset}); }
;

identifier (TmaIdentifier) ::=
	  ID												{ $$ = new TmaIdentifier($ID, source, ${left().offset}, ${left().endoffset}); }
;

symref (TmaSymref) ::=
	  ID												{ $$ = new TmaSymref($ID, source, ${left().offset}, ${left().endoffset}); }
;

type (String) ::=
	  '(' scon ')'										{ $$ = $scon; }
	| '(' type_part_list ')'							{ $$ = source.getText(${first().offset}+1, ${last().endoffset}-1); }
;

type_part_list void ::=
	  type_part_list type_part | type_part ;

type_part void ::=
	  '<' | '>' | '[' | ']' | ID | '*' | '.' | ',' | '?' | '@' | '&' | '(' type_part_list? ')' ;

pattern (TmaPattern) ::=
	  regexp											{ $$ = new TmaPattern($regexp, source, ${left().offset}, ${left().endoffset}); }
;

lexer_parts (List<ITmaLexerPart>) ::=
	  lexer_part 										{ $$ = new ArrayList<ITmaLexerPart>(64); ${left()}.add($lexer_part); }
	| list=lexer_parts lexer_part						{ $list.add($lexer_part); }
	| list=lexer_parts syntax_problem					{ $list.add($syntax_problem); }
;

lexer_part (ITmaLexerPart) ::=
	  state_selector
	| named_pattern
	| lexeme
;

named_pattern (TmaNamedPattern) ::=
	  ID '=' pattern									{ $$ = new TmaNamedPattern($ID, $pattern, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

lexeme (TmaLexeme) ::=
	  identifier typeopt ':' (pattern lexem_transitionopt iconopt lexem_attrsopt commandopt)?
                                                    	{ $$ = new TmaLexeme($identifier, $typeopt, $pattern, $lexem_transitionopt, $iconopt, $lexem_attrsopt, $commandopt, source, ${left().offset}, ${left().endoffset}); }
;

lexem_transition (TmaStateref) ::=
	  '=>' stateref										{ $$ = $1; }
;

lexem_attrs (TmaLexemAttrs) ::=
	  '(' lexem_attribute ')'							{ $$ = $1; }
;

lexem_attribute (TmaLexemAttrs) ::=
	  Lsoft												{ $$ = new TmaLexemAttrs(TmaLexemAttribute.LSOFT, source, ${left().offset}, ${left().endoffset}); }
	| Lclass											{ $$ = new TmaLexemAttrs(TmaLexemAttribute.LCLASS, source, ${left().offset}, ${left().endoffset}); }
	| Lspace											{ $$ = new TmaLexemAttrs(TmaLexemAttribute.LSPACE, source, ${left().offset}, ${left().endoffset}); }
	| Llayout											{ $$ = new TmaLexemAttrs(TmaLexemAttribute.LLAYOUT, source, ${left().offset}, ${left().endoffset}); }
;

state_selector (TmaStateSelector) ::=
	  '[' state_list ']'								{ $$ = new TmaStateSelector($state_list, source, ${left().offset}, ${left().endoffset}); }
;

state_list (List<TmaLexerState>) ::=
	  lexer_state										{ $$ = new ArrayList<Integer>(4); ${left()}.add($lexer_state); }
	| list=state_list ',' lexer_state					{ $list.add($lexer_state); }
;

stateref (TmaStateref) ::=
	  ID                                                { $$ = new TmaStateref($ID, source, ${left().offset}, ${left().endoffset}); }
;

lexer_state (TmaLexerState) ::=
	  identifier ('=>' defaultTransition=stateref)?		{ $$ = new TmaLexerState($identifier, $defaultTransition, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

grammar_parts (List<ITmaGrammarPart>) ::=
	  grammar_part 										{ $$ = new ArrayList<ITmaGrammarPart>(64); ${left()}.add($grammar_part); }
	| list=grammar_parts grammar_part					{ $list.add($grammar_part); }
	| list=grammar_parts syntax_problem					{ $list.add($syntax_problem); }
;

grammar_part (ITmaGrammarPart) ::=
	  nonterm
	| directive
;

nonterm (TmaNonterm) ::=
	  annotations? identifier nonterm_type? '::=' rules ';'
	  													{ $$ = new TmaNonterm($identifier, $nonterm_type, $rules, $annotations, source, ${left().offset}, ${left().endoffset}); }
;

nonterm_type (ITmaNontermType) ::=
	  Lreturns symref                                   { $$ = new TmaNontermTypeAST($symref, source, ${left().offset}, ${left().endoffset}); }
	| Linline? Lclass name=identifieropt				{ $$ = new TmaNontermTypeHint(TmaNontermTypeHint.Kind.${if self.Linline.rightOffset>=0}INLINE_CLASS${else}CLASS${end}, $name, source, ${left().offset}, ${left().endoffset}); }
	| Linterface name=identifieropt						{ $$ = new TmaNontermTypeHint(TmaNontermTypeHint.Kind.INTERFACE, $name, source, ${left().offset}, ${left().endoffset}); }
	| Lvoid												{ $$ = new TmaNontermTypeHint(TmaNontermTypeHint.Kind.VOID, null, source, ${left().offset}, ${left().endoffset}); }
	| type                                              { $$ = new TmaNontermTypeRaw($type, source, ${left().offset}, ${left().endoffset}); }
;

assoc (TmaAssoc) ::=
	  Lleft												{ $$ = TmaAssoc.LLEFT; }
	| Lright											{ $$ = TmaAssoc.LRIGHT; }
	| Lnonassoc											{ $$ = TmaAssoc.LNONASSOC; }
;

directive (ITmaGrammarPart) ::=
	  '%' assoc references ';'							{ $$ = new TmaDirectivePrio($references, $assoc, source, ${left().offset}, ${left().endoffset}); }
	| '%' Linput inputs ';'								{ $$ = new TmaDirectiveInput($inputs, source, ${left().offset}, ${left().endoffset}); }
;

inputs (List<TmaInputref>) ::=
	  inputref											{ $$ = new ArrayList<TmaInputref>(); ${left()}.add($inputref); }
	| list=inputs ',' inputref               			{ $list.add($inputref); }
;

inputref (TmaInputref) ::=
	symref Lnoeoiopt									{ $$ = new TmaInputref($symref, $Lnoeoiopt != null, source, ${left().offset}, ${left().endoffset}); }
;

references (List<TmaSymref>) ::=
	  symref											{ $$ = new ArrayList<TmaSymref>(); ${left()}.add($symref); }
	| list=references symref							{ $list.add($symref); }
;

references_cs (List<TmaSymref>) ::=
	  symref											{ $$ = new ArrayList<TmaSymref>(); ${left()}.add($symref); }
	| list=references_cs ',' symref						{ $list.add($symref); }
;

rules (List<TmaRule0>) ::=
 	rule_list
;

rule_list (List<TmaRule0>) ::=
	  rule0												{ $$ = new ArrayList<TmaRule0>(); ${left()}.add($rule0); }
	| list=rule_list '|' rule0							{ $list.add($rule0); }
;

rule0 (TmaRule0) ::=
	  rhsPrefix? rhsParts? rhsSuffixopt					{ $$ = new TmaRule0($rhsPrefix, $rhsParts, $rhsSuffixopt, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem									{ $$ = new TmaRule0($syntax_problem); }
;

rhsPrefix (TmaRhsPrefix) ::=
	  '[' annotations ']'								{ $$ = new TmaRhsPrefix($annotations, null, source, ${left().offset}, ${left().endoffset}); }
	| '[' annotations? alias=identifier ']'				{ $$ = new TmaRhsPrefix($annotations, $alias, source, ${left().offset}, ${left().endoffset}); }
;

rhsSuffix (TmaRhsSuffix) ::=
	  '%' Lprio symref									{ $$ = new TmaRhsSuffix(TmaRhsSuffix.TmaKindKind.LPRIO, $symref, source, ${left().offset}, ${left().endoffset}); }
	| '%' Lshift symref									{ $$ = new TmaRhsSuffix(TmaRhsSuffix.TmaKindKind.LSHIFT, $symref, source, ${left().offset}, ${left().endoffset}); }
;

rhsParts (List<ITmaRhsPart>) ::=
	  rhsPart											{ $$ = new ArrayList<ITmaRhsPart>(); ${left()}.add($rhsPart); }
	| list=rhsParts rhsPart 							{ $list.add($rhsPart); }
	| list=rhsParts syntax_problem						{ $list.add($syntax_problem); }
;

%left '&';

rhsPart (ITmaRhsPart) ::=
	  rhsAnnotated
	| rhsUnordered
	| command
;

rhsAnnotated (ITmaRhsPart) ::=
	  rhsAssignment
	| annotations rhsAssignment							{ $$ = new TmaRhsAnnotated($annotations, $rhsAssignment, source, ${left().offset}, ${left().endoffset}); }
;

rhsAssignment (ITmaRhsPart) ::=
	  rhsOptional
	| identifier '=' rhsOptional						{ $$ = new TmaRhsAssignment($identifier, false, $rhsOptional, source, ${left().offset}, ${left().endoffset}); }
	| identifier '+=' rhsOptional						{ $$ = new TmaRhsAssignment($identifier, true, $rhsOptional, source, ${left().offset}, ${left().endoffset}); }
;

rhsOptional (ITmaRhsPart) ::=
	  rhsCast
	| rhsCast '?'										{ $$ = new TmaRhsQuantifier($rhsCast, TmaRhsQuantifier.KIND_OPTIONAL, source, ${left().offset}, ${left().endoffset}); }
;

rhsCast (ITmaRhsPart) ::=
	  rhsClass
	| rhsClass Las symref								{ $$ = new TmaRhsCast($rhsClass, $symref, source, ${left().offset}, ${left().endoffset}); }
	| rhsClass Las literal								{ $$ = new TmaRhsAsLiteral($rhsClass, $literal, source, ${left().offset}, ${left().endoffset}); }
;

rhsUnordered (ITmaRhsPart) ::=
	  left=rhsPart '&' right=rhsPart					{ $$ = new TmaRhsUnordered($left, $right, source, ${left().offset}, ${left().endoffset}); }
;

rhsClass (ITmaRhsPart) ::=
	  rhsPrimary
	| identifier ':' rhsPrimary							{ $$ = new TmaRhsClass($identifier, $rhsPrimary, source, ${left().offset}, ${left().endoffset}); }
;

rhsPrimary (ITmaRhsPart) ::=
	  symref											{ $$ = new TmaRhsSymbol($symref, source, ${left().offset}, ${left().endoffset}); }
	| '(' rules ')'										{ $$ = new TmaRhsNested($rules, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '(' rhsParts Lseparator references ')' '+'		{ $$ = new TmaRhsList($rhsParts, $references, true, source, ${left().offset}, ${left().endoffset}); }
	| '(' rhsParts Lseparator references ')' '*'		{ $$ = new TmaRhsList($rhsParts, $references, false, source, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '*'									{ $$ = new TmaRhsQuantifier($rhsPrimary, TmaRhsQuantifier.KIND_ZEROORMORE, source, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '+'									{ $$ = new TmaRhsQuantifier($rhsPrimary, TmaRhsQuantifier.KIND_ONEORMORE, source, ${left().offset}, ${left().endoffset}); }
	| '$' '(' rules (';' brackets=(rhsBracketsPair separator ',')+)? ')'
														{ $$ = new TmaRhsIgnored($rules, $brackets, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsBracketsPair (TmaRhsBracketsPair) ::=
	  lhs=symref '..' rhs=symref						{ $$ = new TmaRhsBracketsPair($lhs, $rhs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

annotations (TmaAnnotations) ::=
	annotation_list										{ $$ = new TmaAnnotations($annotation_list, source, ${left().offset}, ${left().endoffset}); }
;

annotation_list (java.util.@List<TmaAnnotation>) ::=
	  annotation										{ $$ = new java.util.@ArrayList<TmaAnnotation>(); ${left()}.add($annotation); }
	| annotation_list annotation						{ $annotation_list.add($annotation); }
;

annotation (TmaAnnotation) ::=
	  '@' ID ('{' expression '}')?                      { $$ = new TmaAnnotation($ID, $expression, source, ${left().offset}, ${left().endoffset}); }
	| '@' syntax_problem                                { $$ = new TmaAnnotation($syntax_problem, source, ${left().offset}, ${left().endoffset}); }
;

##### EXPRESSIONS

expression (ITmaExpression) ::=
	  literal
	| symref
	| Lnew name '(' map_entriesopt ')'					{ $$ = new TmaInstance($name, $map_entriesopt, source, ${left().offset}, ${left().endoffset}); }
	| '[' expression_listopt ']'						{ $$ = new TmaArray($expression_listopt, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem
;

literal (TmaLiteral) ::=
	  scon                                              { $$ = new TmaLiteral($scon, source, ${left().offset}, ${left().endoffset}); }
	| icon                                              { $$ = new TmaLiteral($icon, source, ${left().offset}, ${left().endoffset}); }
	| Ltrue                                             { $$ = new TmaLiteral(Boolean.TRUE, source, ${left().offset}, ${left().endoffset}); }
	| Lfalse                                            { $$ = new TmaLiteral(Boolean.FALSE, source, ${left().offset}, ${left().endoffset}); }
;

expression_list (List<ITmaExpression>) ::=
	expression											{ $$ = new ArrayList(); ${left()}.add($expression); }
	| expression_list ',' expression					{ $expression_list.add($expression); }
;

map_entries (java.util.@List<TmaMapEntriesItem>) ::=
	  ID map_separator expression						{ $$ = new java.util.@ArrayList<TmaMapEntriesItem>(); ${left()}.add(new TmaMapEntriesItem($ID, $expression, source, ${left().offset}, ${left().endoffset})); }
	| map_entries ',' ID map_separator expression		{ $map_entries.add(new TmaMapEntriesItem($ID, $expression, source, ${ID.offset}, ${left().endoffset})); }
;

map_separator void ::=
	':' | '=' | '=>' ;

name (TmaName) ::=
	qualified_id 										{ $$ = new TmaName($qualified_id, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID								{ $$ = $qualified_id + "." + $ID; }
;

command (TmaCommand) ::=
	code												{ $$ = new TmaCommand(source, ${first().offset}+1, ${last().endoffset}-1); }
;

syntax_problem (TmaSyntaxProblem) ::=
	error												{ $$ = new TmaSyntaxProblem(source, ${left().offset}, ${left().endoffset}); }
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
		@Override
		public void error(String message, int line, int offset) {
			reporter.error(message, line, offset, offset + 1);
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