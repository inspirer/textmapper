#  syntax: lalr1 generator source grammar

#  Copyright 2002-2014 Evgeny Gryaznov
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

:: lexer

[initial, afterAt => initial, afterAtID => initial]

regexp(String):	/\/([^\/\\\n]|\\.)*\//	{ $$ = tokenText().substring(1, tokenSize()-1); }
scon(String):	/"([^\n\\"]|\\.)*"/		{ $$ = unescape(tokenText(), 1, tokenSize()-1); }
icon(Integer):	/-?[0-9]+/				{ $$ = Integer.parseInt(tokenText()); }

eoi:           /%%.*(\r?\n)?/			{ templatesStart = token.endoffset; }
_skip:         /[\n\r\t ]+/		(space)
_skip_comment:  /#.*(\r?\n)?/			{ spaceToken = skipComments; }

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
'*':	/*/
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

Lprio:  /prio/				(soft)
Lshift: /shift/				(soft)

Lreturns: /returns/			(soft)

Linput: /input/				(soft)
Lleft:  /left/				(soft)
Lright: /right/				(soft)
Lnonassoc: /nonassoc/		(soft)

Lparam: /param/			    (soft)
Lstring: /string/			(soft)
Lbool: /bool/				(soft)
Lint: /int/					(soft)
Lsymbol: /symbol/			(soft)

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

input (TmaInput) ::=
	  header importsopt options? lexer_section parser_section?
	  													{ $$ = new TmaInput($header, $importsopt, $options, $lexer_section, $parser_section, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

header (TmaHeader) ::=
	  Llanguage name ('(' target=name ')')? parsing_algorithmopt ';'
														{ $$ = new TmaHeader($name, $target, $parsing_algorithmopt, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

lexer_section (List<ITmaLexerPart>) ::=
	  '::' Llexer lexer_parts							{ $$ = $2; }
;

parser_section (List<ITmaGrammarPart>) ::=
	  '::' Lparser grammar_parts						{ $$ = $2; }
;

# ignored
parsing_algorithm (TmaParsingAlgorithm) ::=
	  Llalr '(' la=icon ')'								{ $$ = new TmaParsingAlgorithm($la, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

imports (List<TmaImport>) ::=
	  import_											{ $$ = new ArrayList<TmaImport>(16); ${left()}.add($import_); }
	| list=imports import_								{ $list.add($import_); }
;


import_ (TmaImport) ::=
	  Limport alias=ID? file=scon ';'					{ $$ = new TmaImport($alias, $file, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;


options (List<TmaOption>) ::=
	  option											{ $$ = new ArrayList<TmaOption>(16); ${left()}.add($option); }
	| list=options option								{ $list.add($option); }
;

option (TmaOption) ::=
	  ID '=' expression 								{ $$ = new TmaOption($ID, $expression, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| syntax_problem									{ $$ = new TmaOption(null, null, $syntax_problem, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

identifier (TmaIdentifier) ::=
	  ID												{ $$ = new TmaIdentifier($ID, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

symref (TmaSymref) ::=
	  ID symref_args?									{ $$ = new TmaSymref($ID, $symref_args, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

symref_noargs (TmaSymref) ::=
	  ID												{ $$ = new TmaSymref($ID, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
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
	  regexp											{ $$ = new TmaPattern($regexp, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
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
	| lexer_directive
;

named_pattern (TmaNamedPattern) ::=
	  ID '=' pattern									{ $$ = new TmaNamedPattern($ID, $pattern, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

lexeme (TmaLexeme) ::=
	  identifier typeopt ':' (pattern lexeme_transitionopt iconopt lexeme_attrsopt commandopt)?
                                                    	{ $$ = new TmaLexeme($identifier, $typeopt, $pattern, $lexeme_transitionopt, $iconopt, $lexeme_attrsopt, $commandopt, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

lexeme_transition (TmaStateref) ::=
	  '=>' stateref										{ $$ = $1; }
;

lexeme_attrs (TmaLexemeAttrs) ::=
	  '(' lexeme_attribute ')'							{ $$ = $1; }
;

lexeme_attribute (TmaLexemeAttrs) ::=
	  Lsoft												{ $$ = new TmaLexemeAttrs(TmaLexemeAttribute.LSOFT, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Lclass											{ $$ = new TmaLexemeAttrs(TmaLexemeAttribute.LCLASS, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Lspace											{ $$ = new TmaLexemeAttrs(TmaLexemeAttribute.LSPACE, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Llayout											{ $$ = new TmaLexemeAttrs(TmaLexemeAttribute.LLAYOUT, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

lexer_directive (TmaDirectiveBrackets) ::=
	  '%' Lbrackets opening=symref_noargs closing=symref_noargs ';'
														{ $$ = new TmaDirectiveBrackets($opening, $closing, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

state_selector (TmaStateSelector) ::=
	  '[' state_list ']'								{ $$ = new TmaStateSelector($state_list, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

state_list (List<TmaLexerState>) ::=
	  lexer_state										{ $$ = new ArrayList<Integer>(4); ${left()}.add($lexer_state); }
	| list=state_list ',' lexer_state					{ $list.add($lexer_state); }
;

stateref (TmaStateref) ::=
	  ID                                                { $$ = new TmaStateref($ID, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
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
	| nonterm_param
	| directive
;

nonterm (TmaNonterm) ::=
	  annotations? identifier nonterm_params? nonterm_type? '::=' rules ';'
	  													{ $$ = new TmaNonterm($annotations, $identifier, $nonterm_params, $nonterm_type, $rules, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

nonterm_type (ITmaNontermType) ::=
	  Lreturns symref_noargs                            { $$ = new TmaNontermTypeAST($symref_noargs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Linline? Lclass name=identifieropt implementsopt
														{ $$ = new TmaNontermTypeHint(${self.Linline.rightOffset>=0 ? 'true' : 'false'}, TmaNontermTypeHint.TmaKindKind.LCLASS, $name, $implementsopt, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Linterface name=identifieropt implementsopt
														{ $$ = new TmaNontermTypeHint(false, TmaNontermTypeHint.TmaKindKind.LINTERFACE, $name, $implementsopt, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Lvoid												{ $$ = new TmaNontermTypeHint(false, TmaNontermTypeHint.TmaKindKind.LVOID, null, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| type                                              { $$ = new TmaNontermTypeRaw($type, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

implements (List<TmaSymref>) ::=
	  ':' references_cs									{ $$ = $1; }
;

assoc (TmaAssoc) ::=
	  Lleft												{ $$ = TmaAssoc.LLEFT; }
	| Lright											{ $$ = TmaAssoc.LRIGHT; }
	| Lnonassoc											{ $$ = TmaAssoc.LNONASSOC; }
;

nonterm_param (ITmaGrammarPart) ::=
	  '%' Lparam identifier param_type ('=' param_value)? ';'
														{ $$ = new TmaNontermParam($identifier, $param_type, $param_value, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

directive (ITmaGrammarPart) ::=
	  '%' assoc references ';'							{ $$ = new TmaDirectivePrio($assoc, $references, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '%' Linput inputs ';'								{ $$ = new TmaDirectiveInput($inputs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

inputs (List<TmaInputref>) ::=
	  inputref											{ $$ = new ArrayList<TmaInputref>(); ${left()}.add($inputref); }
	| list=inputs ',' inputref               			{ $list.add($inputref); }
;

inputref (TmaInputref) ::=
	symref_noargs Lnoeoiopt								{ $$ = new TmaInputref($symref_noargs, $Lnoeoiopt != null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

references (List<TmaSymref>) ::=
	  symref_noargs										{ $$ = new ArrayList<TmaSymref>(); ${left()}.add($symref_noargs); }
	| list=references symref_noargs						{ $list.add($symref_noargs); }
;

references_cs (List<TmaSymref>) ::=
	  symref_noargs										{ $$ = new ArrayList<TmaSymref>(); ${left()}.add($symref_noargs); }
	| list=references_cs ',' symref_noargs				{ $list.add($symref_noargs); }
;

rules (List<TmaRule0>) ::=
 	rule_list
;

rule_list (List<TmaRule0>) ::=
	  rule0												{ $$ = new ArrayList<TmaRule0>(); ${left()}.add($rule0); }
	| list=rule_list '|' rule0							{ $list.add($rule0); }
;

rule0 (TmaRule0) ::=
	  rhsPrefix? rhsParts? ruleAction? rhsSuffixopt		{ $$ = new TmaRule0($rhsPrefix, $rhsParts, $ruleAction, $rhsSuffixopt, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| error=syntax_problem								{ $$ = new TmaRule0(null, null, null, null, $error, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

predicate (ITmaPredicateExpression) ::=
	  '[' predicate_expression ']'						{ $$ = $1; }
;

rhsPrefix (TmaRhsPrefix) ::=
	  predicate? annotations ':'						{ $$ = new TmaRhsPrefix($predicate, $annotations, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| predicate ':'										{ $$ = new TmaRhsPrefix($predicate, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsSuffix (TmaRhsSuffix) ::=
	  '%' Lprio symref_noargs							{ $$ = new TmaRhsSuffix(TmaRhsSuffix.TmaKindKind.LPRIO, $symref_noargs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '%' Lshift symref_noargs							{ $$ = new TmaRhsSuffix(TmaRhsSuffix.TmaKindKind.LSHIFT, $symref_noargs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

ruleAction (TmaRuleAction) ::=
	  '{~' action=identifier parameter=scon? '}'		{ $$ = new TmaRuleAction($action, $parameter, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsParts (List<ITmaRhsPart>) ::=
	  rhsPart											{ $$ = new ArrayList<ITmaRhsPart>(); ${left()}.add($rhsPart); }
	| list=rhsParts rhsPart 							{ $list.add($rhsPart); }
	| list=rhsParts syntax_problem						{ $list.add($syntax_problem); }
;

%left '|';
%left '&';

rhsPart (ITmaRhsPart) ::=
	  rhsAnnotated
	| rhsUnordered
	| command
;

rhsAnnotated (ITmaRhsPart) ::=
	  rhsAssignment
	| annotations rhsAssignment							{ $$ = new TmaRhsAnnotated($annotations, $rhsAssignment, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsAssignment (ITmaRhsPart) ::=
	  rhsOptional
	| identifier '=' rhsOptional						{ $$ = new TmaRhsAssignment($identifier, false, $rhsOptional, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| identifier '+=' rhsOptional						{ $$ = new TmaRhsAssignment($identifier, true, $rhsOptional, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsOptional (ITmaRhsPart) ::=
	  rhsCast
	| rhsCast '?'										{ $$ = new TmaRhsQuantifier($rhsCast, TmaRhsQuantifier.TmaQuantifierKind.QUESTIONMARK, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsCast (ITmaRhsPart) ::=
	  rhsClass
	| rhsClass Las symref								{ $$ = new TmaRhsCast($rhsClass, $symref, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| rhsClass Las literal								{ $$ = new TmaRhsAsLiteral($rhsClass, $literal, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsUnordered (ITmaRhsPart) ::=
	  left=rhsPart '&' right=rhsPart					{ $$ = new TmaRhsUnordered($left, $right, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsClass (ITmaRhsPart) ::=
	  rhsPrimary
	| identifier ':' rhsPrimary							{ $$ = new TmaRhsClass($identifier, $rhsPrimary, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

rhsPrimary (ITmaRhsPart) ::=
	  symref											{ $$ = new TmaRhsSymbol($symref, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '(' rules ')'										{ $$ = new TmaRhsNested($rules, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '(' rhsParts Lseparator references ')' '+'		{ $$ = new TmaRhsList($rhsParts, $references, true, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '(' rhsParts Lseparator references ')' '*'		{ $$ = new TmaRhsList($rhsParts, $references, false, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '*'									{ $$ = new TmaRhsQuantifier($rhsPrimary, TmaRhsQuantifier.TmaQuantifierKind.MULT, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| rhsPrimary '+'									{ $$ = new TmaRhsQuantifier($rhsPrimary, TmaRhsQuantifier.TmaQuantifierKind.PLUS, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '$' '(' rules ')'									{ $$ = new TmaRhsIgnored($rules, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Lset '(' expr=setExpression ')'					{ $$ = new TmaRhsSet($expr, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

setPrimary (ITmaSetExpression) ::=
	  operator=ID? symbol=symref						{ $$ = new TmaSetSymbol($operator, $symbol, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '(' inner=setExpression ')'						{ $$ = new TmaSetCompound($inner, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '~' inner=setPrimary								{ $$ = new TmaSetComplement($inner, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

setExpression (ITmaSetExpression) ::=
	  setPrimary
	| left=setExpression kind='|' right=setExpression	{ $$ = new TmaSetBinary($left, TmaSetBinary.TmaKindKind.OR, $right, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| left=setExpression kind='&' right=setExpression	{ $$ = new TmaSetBinary($left, TmaSetBinary.TmaKindKind.AMPERSAND, $right, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

annotations (TmaAnnotations) ::=
	annotation_list										{ $$ = new TmaAnnotations($annotation_list, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

annotation_list (java.util.@List<TmaAnnotation>) ::=
	  annotation										{ $$ = new java.util.@ArrayList<TmaAnnotation>(); ${left()}.add($annotation); }
	| annotation_list annotation						{ $annotation_list.add($annotation); }
;

annotation (TmaAnnotation) ::=
	  '@' ID ('{' expression '}')?                      { $$ = new TmaAnnotation($ID, $expression, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '@' syntax_problem                                { $$ = new TmaAnnotation(null, null, $syntax_problem, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

##### Nonterminal parameters

nonterm_params (TmaNontermParams) ::=
	  '<' refs=(param_ref separator ',')+ '>'			{ $$ = new TmaNontermParams($refs, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

param_ref (TmaIdentifier) ::=
	  identifier ;

symref_args (TmaSymrefArgs) ::=
	  '<' value_list=(param_value separator ',')+ '>'	{ $$ = new TmaSymrefArgs($value_list, null, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '<' keyvalue_list=(keyval_arg separator ',')* '>'	{ $$ = new TmaSymrefArgs(null, $keyvalue_list, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

keyval_arg (TmaKeyvalArg) ::=
	  param_ref ':' param_value							{ $$ = new TmaKeyvalArg($param_ref, $param_value, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

param_type (TmaParamType) ::=
	  Lint												{ $$ = TmaParamType.LINT; }
	| Lstring											{ $$ = TmaParamType.LSTRING; }
	| Lbool                                             { $$ = TmaParamType.LBOOL; }
	| Lsymbol                                           { $$ = TmaParamType.LSYMBOL; }
;

param_value (ITmaParamValue) ::=
	  literal
	| symref_noargs
;

predicate_primary (ITmaPredicateExpression) ::=
	  is_negated='!'? param_ref							{ $$ = new TmaBoolPredicate(${is_negated.rightOffset >= 0 ? 'true' : 'false'}, $param_ref, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| param_ref '==' literal
														{ $$ = new TmaComparePredicate($param_ref, TmaComparePredicate.TmaKindKind.EQUAL_EQUAL, $literal, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| param_ref '!=' literal
														{ $$ = new TmaComparePredicate($param_ref, TmaComparePredicate.TmaKindKind.EXCLAMATION_EQUAL, $literal, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

%left '||';
%left '&&';

predicate_expression (ITmaPredicateExpression) ::=
	  predicate_primary
	| left=predicate_expression kind='&&' right=predicate_expression	{ $$ = new TmaPredicateBinary($left, TmaPredicateBinary.TmaKindKind.AMPERSAND_AMPERSAND, $right, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| left=predicate_expression kind='||' right=predicate_expression	{ $$ = new TmaPredicateBinary($left, TmaPredicateBinary.TmaKindKind.OR_OR, $right, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

##### EXPRESSIONS

# TODO use json, get rid of new & symref

expression (ITmaExpression) ::=
	  literal
	| symref
	| Lnew name '(' entries=(map_entry separator ',')* ')'
														{ $$ = new TmaInstance($name, $entries, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| '[' content=(expression separator ',')* ']'		{ $$ = new TmaArray($content, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| syntax_problem
;

map_entry (TmaMapEntry) ::=
	  ID ':' expression									{ $$ = new TmaMapEntry($ID, $expression, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

literal (TmaLiteral) ::=
	  scon                                              { $$ = new TmaLiteral($scon, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| icon                                              { $$ = new TmaLiteral($icon, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Ltrue                                             { $$ = new TmaLiteral(Boolean.TRUE, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
	| Lfalse                                            { $$ = new TmaLiteral(Boolean.FALSE, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

expression_list (List<ITmaExpression>) ::=
	expression											{ $$ = new ArrayList(); ${left()}.add($expression); }
	| expression_list ',' expression					{ $expression_list.add($expression); }
;

name (TmaName) ::=
	qualified_id 										{ $$ = new TmaName($qualified_id, source, ${left().line}, ${left().offset}, ${left().endoffset}); }
;

qualified_id (String) ::=
	  ID
	| qualified_id '.' ID								{ $$ = $qualified_id + "." + $ID; }
;

command (TmaCommand) ::=
	code												{ $$ = new TmaCommand(source, ${left().line}, ${first().offset}+1, ${last().endoffset}-1); }
;

syntax_problem (TmaSyntaxProblem) ::=
	error												{ $$ = new TmaSyntaxProblem(source, ${left().line}, ${left().offset}, ${left().endoffset}); }
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