#  syntax: lalr1 generator source grammar

#  Copyright 2002-2012 Evgeny Gryaznov
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
prefix = "Lapg"
package = "org.textway.lapg.parser"
maxtoken = 2048
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCleanup = true
genCopyright = true

# Vocabulary

error:

[0]

identifier(String): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)
			{ $lexem = current(); break; }

regexp(String):	/\/([^\/\\\n]|\\.)*\//	{ $lexem = token.toString().substring(1, token.length()-1); break; }
scon(String):	/"([^\n\\"]|\\.)*"/		{ $lexem = unescape(current(), 1, token.length()-1); break; }
icon(Integer):	/-?[0-9]+/				{ $lexem = Integer.parseInt(current()); break; }

eoi:           /\n%%.*/					{ templatesStart = lapg_n.endoffset; break; }
'%':           /\n%|%/
_skip:         /\n|[\t\r ]+/    		{ return false; }
_skip_comment:  /#.*/					{ return !skipComments; }

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
')':	/\)/
'<':	/</
'>':	/>/
'*':	/*/
'+':	/+/
'?':	/?/
'?!':	/?!/
'&':	/&/
'@':	/@/

Ltrue:  /true/
Lfalse: /false/

Lprio:  /prio/				(soft)
Lshift: /shift/				(soft)


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

code:	/\{/			{ skipAction(); lapg_n.endoffset = getOffset(); break; }

# Grammar

%input input, expression;

input (AstRoot) ::=
	options lexer_parts grammar_partsopt				{  $$ = new AstRoot($options, $lexer_parts, $grammar_partsopt, source, ${input.offset}, ${input.endoffset}); }
	| lexer_parts grammar_partsopt						{  $$ = new AstRoot(null, $lexer_parts, $grammar_partsopt, source, ${input.offset}, ${input.endoffset}); }
;

options (List<AstOptionPart>) ::=
	  option											{ $$ = new ArrayList<AstOptionPart>(16); $options.add($option); }
	| list=options option								{ $list.add($option); } 
;

option (AstOptionPart) ::=
	  identifier '=' expression 						{ $$ = new AstOption($identifier, $expression, source, ${option.offset}, ${option.endoffset}); }
	| syntax_problem
;

symbol (AstIdentifier) ::=
	identifier											{ $$ = new AstIdentifier($identifier, source, ${symbol.offset}, ${symbol.endoffset}); } 
;

reference (AstReference) ::=
	identifier											{ $$ = new AstReference($identifier, source, ${reference.offset}, ${reference.endoffset}); }
;

type (String) ::=
	  '(' scon ')'										{ $$ = $scon; }
	| '(' type_part_list ')'							{ $$ = source.getText(${self[0].offset}+1, ${self[2].endoffset}-1); }
;

type_part_list ::=
	type_part_list type_part | type_part ;

type_part ::=
	'<' | '>' | '[' | ']' | identifier | '*' | '.' | ',' | '?' | '@' | '&' | '(' type_part_listopt ')'
;

pattern (AstRegexp) ::=
	regexp												{ $$ = new AstRegexp($regexp, source, ${pattern.offset}, ${pattern.endoffset}); }
;

lexer_parts (List<AstLexerPart>) ::= 
	  lexer_part 										{ $$ = new ArrayList<AstLexerPart>(64); $lexer_parts.add($lexer_part); }
	| list=lexer_parts lexer_part						{ $list.add($lexer_part); }
	| list=lexer_parts syntax_problem					{ $list.add($syntax_problem); }
;

lexer_part (AstLexerPart) ::=
	  group_selector: '[' icon_list ']'					{ $$ = new AstGroupsSelector($icon_list, source, ${lexer_part.offset}, ${lexer_part.endoffset}); }
	| alias: identifier '=' pattern						{ $$ = new AstNamedPattern($identifier, $pattern, source, ${lexer_part.offset}, ${lexer_part.endoffset}); }
	| symbol typeopt ':'								{ $$ = new AstLexeme($symbol, $typeopt, null, null, null, null, source, ${lexer_part.offset}, ${lexer_part.endoffset}); }
	| symbol typeopt ':' pattern iconopt lexem_attrsopt commandopt
														{ $$ = new AstLexeme($symbol, $typeopt, $pattern, $iconopt, $lexem_attrsopt, $commandopt, source, ${lexer_part.offset}, ${lexer_part.endoffset}); }
;

lexem_attrs (AstLexemAttrs) ::=
	  '(' lexem_attribute ')'							{ $$ = $1; }
;

lexem_attribute (AstLexemAttrs) ::=
	  Lsoft												{ $$ = new AstLexemAttrs(org.textway.lapg.api.@Lexem.KIND_SOFT, source, ${left().offset}, ${left().endoffset}); }
	| Lclass											{ $$ = new AstLexemAttrs(org.textway.lapg.api.@Lexem.KIND_CLASS, source, ${left().offset}, ${left().endoffset}); }
	| Lspace											{ $$ = new AstLexemAttrs(org.textway.lapg.api.@Lexem.KIND_SPACE, source, ${left().offset}, ${left().endoffset}); }
	| Llayout											{ $$ = new AstLexemAttrs(org.textway.lapg.api.@Lexem.KIND_LAYOUT, source, ${left().offset}, ${left().endoffset}); }
;

icon_list (List<Integer>) ::=
	  icon 												{ $$ = new ArrayList<Integer>(4); $icon_list.add($icon); } 
	| list=icon_list icon  								{ $list.add($icon); }
;

grammar_parts (List<AstGrammarPart>) ::=
	  grammar_part 										{ $$ = new ArrayList<AstGrammarPart>(64); $grammar_parts.add($grammar_part); }
	| list=grammar_parts grammar_part					{ $list.add($grammar_part); }
	| list=grammar_parts syntax_problem					{ $list.add($syntax_problem); }
;

grammar_part (AstGrammarPart) ::= 
	  symbol typeopt '::=' rules ';'					{ $$ = new AstNonTerm($symbol, $typeopt, $rules, null, source, ${grammar_part.offset}, ${grammar_part.endoffset}); }
	| annotations symbol typeopt '::=' rules ';'		{ $$ = new AstNonTerm($symbol, $typeopt, $rules, $annotations, source, ${grammar_part.offset}, ${grammar_part.endoffset}); }
	| directive: directive								{ $$ = $directive; }
;

priority_kw (String) ::=
	Lleft | Lright | Lnonassoc ;

directive ::=
	  '%' priority_kw references ';'					{ $$ = new AstDirective($priority_kw, $references, source, ${left().offset}, ${left().endoffset}); }
	| '%' Linput inputs ';'								{ $$ = new AstInputDirective($inputs, source, ${left().offset}, ${left().endoffset}); }
;

inputs (List<AstInputRef>) ::=
	  inputref											{ $$ = new ArrayList<AstInputRef>(); $inputs.add($inputref); }
	| list=inputs ',' inputref               			{ $list.add($inputref); }
;

inputref (AstInputRef) ::=
	reference Lnoeoiopt									{ $$ = new AstInputRef($reference, $Lnoeoiopt != null, source, ${left().offset}, ${left().endoffset}); }
;

references (List<AstReference>) ::= 
	  reference 										{ $$ = new ArrayList<AstReference>(); $references.add($reference); }
	| list=references reference							{ $list.add($reference); }
;

rules (List<AstRule>) ::= 
	  rule0												{ $$ = new ArrayList<AstRule>(); $rules.add($rule0); }
	| list=rules '|' rule0								{ $list.add($rule0); }
;

rule0 (AstRule) ::=
	  ruleprefix ruleparts rule_attrsopt				{ $$ = new AstRule($ruleprefix, $ruleparts, $rule_attrsopt, source, ${rule0.offset}, ${rule0.endoffset}); }
	| 			 ruleparts rule_attrsopt				{ $$ = new AstRule(null, $ruleparts, $rule_attrsopt, source, ${rule0.offset}, ${rule0.endoffset}); }
	| ruleprefix rule_attrsopt  						{ $$ = new AstRule($ruleprefix, null, $rule_attrsopt, source, ${rule0.offset}, ${rule0.endoffset}); }
	| 			 rule_attrsopt  						{ $$ = new AstRule(null, null, $rule_attrsopt, source, ${rule0.offset}, ${rule0.endoffset}); }
	| syntax_problem									{ $$ = new AstRule($syntax_problem); }
;

ruleprefix (AstRulePrefix) ::=
	  annotations ':'									{ $$ = new AstRulePrefix($annotations, null); }
	| ruleannotations identifier ':'					{ $$ = new AstRulePrefix($ruleannotations, $identifier); }
	| identifier ':'									{ $$ = new AstRulePrefix(null, $identifier); }
;

ruleparts (List<AstRulePart>) ::=
	  rulepart											{ $$ = new ArrayList<AstRulePart>(); $ruleparts.add($rulepart); }
	| list=ruleparts rulepart 							{ $list.add($rulepart); }
	| list=ruleparts syntax_problem						{ $list.add($syntax_problem); }
;

%left '&';
%nonassoc '?' '*' '+';

rulepart (AstRulePart) ::=
	  ruleannotations identifier '=' reference			{ $$ = new AstRuleSymbol($identifier, $reference, $ruleannotations, source, ${rulepart.offset}, ${rulepart.endoffset}); }
	| ruleannotations reference 						{ $$ = new AstRuleSymbol(null, $reference, $ruleannotations, source, ${rulepart.offset}, ${rulepart.endoffset}); }
	| identifier '=' reference							{ $$ = new AstRuleSymbol($identifier, $reference, null, source, ${rulepart.offset}, ${rulepart.endoffset}); }
	| reference 										{ $$ = new AstRuleSymbol(null, $reference, null, source, ${rulepart.offset}, ${rulepart.endoffset}); }
	| command

	| '(' ruleparts_choice ')'							{ reporter.error(${context->java.err_location('lapg_gg', 'lapg_lexer') }"unsupported, TODO"); }
	| rulepart '&' rulepart								{ reporter.error(${context->java.err_location('lapg_gg', 'lapg_lexer') }"unsupported, TODO"); }
	| rulepart '?'										{ reporter.error(${context->java.err_location('lapg_gg', 'lapg_lexer') }"unsupported, TODO"); }
	| rulepart '*'										{ reporter.error(${context->java.err_location('lapg_gg', 'lapg_lexer') }"unsupported, TODO"); }
	| rulepart '+'										{ reporter.error(${context->java.err_location('lapg_gg', 'lapg_lexer') }"unsupported, TODO"); }
;

ruleparts_choice ::=
	  ruleparts
	| ruleparts_choice '|' ruleparts
;

ruleannotations (AstRuleAnnotations) ::=
	  annotation_list									{ $$ = new AstRuleAnnotations(null, $annotation_list, source, ${left().offset}, ${left().endoffset}); }
	| negative_la annotation_list						{ $$ = new AstRuleAnnotations($negative_la, $annotation_list, source, ${left().offset}, ${left().endoffset}); }
	| negative_la										{ $$ = new AstRuleAnnotations($negative_la, null, source, ${left().offset}, ${left().endoffset}); }
;

annotations (AstAnnotations) ::=
	annotation_list										{ $$ = new AstAnnotations($annotation_list, source, ${left().offset}, ${left().endoffset}); }
;

annotation_list (java.util.@List<AstNamedEntry>) ::=
	  annotation										{ $$ = new java.util.@ArrayList<AstNamedEntry>(); $annotation_list.add($annotation); }
	| annotation_list annotation						{ $annotation_list#0.add($annotation); }
;

annotation (AstNamedEntry) ::=
	  '@' identifier 									{ $$ = new AstNamedEntry($identifier, null, source, ${left().offset}, ${left().endoffset}); }
	| '@' identifier '(' expression ')'					{ $$ = new AstNamedEntry($identifier, $expression, source, ${left().offset}, ${left().endoffset}); }
	| '@' syntax_problem								{ $$ = new AstNamedEntry($syntax_problem); }
;

negative_la (AstNegativeLA) ::=
	'(' '?!' negative_la_clause ')'						{ $$ = new AstNegativeLA($negative_la_clause, source, ${left().offset}, ${left().endoffset}); }
;

negative_la_clause (java.util.@List<AstReference>) ::=
	  reference											{ $$ = new java.util.@ArrayList<AstReference>(); $negative_la_clause.add($reference); }
	| negative_la_clause '|' reference					{ $negative_la_clause#0.add($reference); }
;

##### EXPRESSIONS

expression (AstExpression) ::=
	  scon                                              { $$ = new AstLiteralExpression($scon, source, ${left().offset}, ${left().endoffset}); }
	| icon                                              { $$ = new AstLiteralExpression($icon, source, ${left().offset}, ${left().endoffset}); }
	| Ltrue                                             { $$ = new AstLiteralExpression(Boolean.TRUE, source, ${left().offset}, ${left().endoffset}); }
	| Lfalse                                            { $$ = new AstLiteralExpression(Boolean.FALSE, source, ${left().offset}, ${left().endoffset}); }
	| reference
	| name '(' map_entriesopt ')'						{ $$ = new AstInstance($name, $map_entriesopt, source, ${left().offset}, ${left().endoffset}); }
	| '[' expression_listopt ']'						{ $$ = new AstArray($expression_listopt, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem
;

expression_list (List<AstExpression>) ::=
	expression											{ $$ = new ArrayList(); $expression_list.add($expression); }
	| expression_list ',' expression					{ $expression_list#0.add($expression); }
;

map_entries (java.util.@List<AstNamedEntry>) ::=
	  identifier map_separator expression				{ $$ = new java.util.@ArrayList<AstNamedEntry>(); $map_entries.add(new AstNamedEntry($identifier, $expression, source, ${left().offset}, ${left().endoffset})); }
	| map_entries ',' identifier map_separator expression	{ $map_entries#0.add(new AstNamedEntry($identifier, $expression, source, ${identifier.offset}, ${expression.endoffset})); }
;

map_separator ::=
	':' | '=' | '=>' ;

name (AstName) ::=
	qualified_id 										{ $$ = new AstName($qualified_id, source, ${left().offset}, ${left().endoffset}); }
;

qualified_id (String) ::=
	  identifier
	| qualified_id '.' identifier						{ $$ = $qualified_id#1 + "." + $identifier; }
;


rule_attrs (AstRuleAttribute) ::=
	'%' Lprio reference									{ $$ = new AstPrioClause($reference, source, ${left().offset}, ${left().endoffset}); }
	| '%' Lshift										{ $$ = new AstShiftClause(source, ${left().offset}, ${left().endoffset}); }
;

command (AstCode) ::=
	code												{ $$ = new AstCode(source, ${code.offset}+1, ${code.endoffset}-1); }
;

syntax_problem (AstError) ::=
	error												{ $$ = new AstError(source, ${self[0].offset}, ${self[0].endoffset}); }
;

##################################################################################

%%

${template java.imports-}
${call base-}
import java.util.List;
import java.util.ArrayList;
import org.textway.lapg.parser.ast.*;
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
	org.textway.lapg.parser.action.@SActionLexer.ErrorReporter innerreporter = new org.textway.lapg.parser.action.@SActionLexer.ErrorReporter() {
		public void error(int start, int line, String s) {
			reporter.error(start, start + 1, line, s);
		}
	};
	org.textway.lapg.parser.action.@SActionLexer l = new org.textway.lapg.parser.action.@SActionLexer(innerreporter) {
		@Override
		protected char nextChar() throws java.io.@IOException {
			if (ind[0] < 2) {
				return ind[0]++ == 0 ? '{' : chr;
			}
			LapgLexer.this.advance();
			return chr;
		}
	};
	org.textway.lapg.parser.action.@SActionParser p = new org.textway.lapg.parser.action.@SActionParser(innerreporter);
	try {
		p.parse(l);
	} catch (org.textway.lapg.parser.action.@SActionParser.ParseException e) {
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
org.textway.lapg.parser.LapgTree.@TextSource source;
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