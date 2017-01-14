#  syntax: lalr1 generator source grammar

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

language textmapper(go);

lang = "tm"
package = "github.com/inspirer/textmapper/tm-go/parsers/tm"
eventBased = true
eventFields = true
reportTokens = [invalid_token, multiline_comment, comment]

:: lexer

[initial, afterColonOrEq, afterGT]

reClass = /\[([^\n\r\]\\]|\\.)*\]/
reFirst = /[^\n\r\*\[\\\/]|\\.|{reClass}/
reChar = /{reFirst}|\*/

scon:    /"([^\n\\"]|\\.)*"/
icon:    /-?[0-9]+/

eoi:        /%%.*(\r?\n)?/
whitespace: /[\n\r\t ]+/                      (space)
comment:    /(#|\/\/)[^\r\n]*/                (space)

commentChars = /([^*]|\*+[^*\/])*\**/
multiline_comment: /\/\*{commentChars}\*\//   (space)

'%':    /%/
'::=':  /::=/
'::':   /::/
'|':    /\|/
'||':   /\|\|/
'=':    /=/
'==':   /==/
'!=':   /!=/
'=>':   /=>/
';':    /;/
'.':    /\./
',':    /,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':    /\(/
# TODO overlaps with ID
'->':   /->/
')':    /\)/
'{~':   /\{~/
'}':    /\}/
'<':    /</
'>':    />/
'*':    /\*/
'+':    /+/
'+=':   /+=/
'?':    /?/
'!':    /!/
'~':    /~/
'&':    /&/
'&&':   /&&/
'$':    /$/
'@':    /@/

error:
invalid_token:

ID: /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)

'as': /as/
'false': /false/
'import': /import/
'separator': /separator/
'set': /set/
'true':  /true/

# Soft keywords.

'assert': /assert/
'brackets': /brackets/
'class': /class/
'empty': /empty/
'explicit': /explicit/
'flag': /flag/
'generate': /generate/
'global': /global/
'inline': /inline/
'input': /input/
'interface': /interface/
'lalr': /lalr/
'language': /language/
'layout': /layout/
'left':  /left/
'lexer': /lexer/
'lookahead': /lookahead/
'no-eoi': /no-eoi/
'nonassoc': /nonassoc/
'nonempty': /nonempty/
'param': /param/
'parser': /parser/
'prec':  /prec/
'returns': /returns/
'right': /right/
'shift': /shift/
'soft': /soft/
'space': /space/
'void': /void/

[initial, afterColonOrEq]

code:   /\{[^\{\}]*\}/    /* TODO */

[afterGT]
'{':	/\{/

[afterColonOrEq]
regexp: /\/{reFirst}{reChar}*\//

[initial, afterGT]
'/':    /\//

:: parser

%flag OrSyntaxError = false;

# Basic nonterminals.

identifier<flag KW = false> ::=
    ID

  # Soft keywords
  | 'brackets' | 'inline'   | 'prec'     | 'shift'     | 'returns' | 'input'
  | 'left'     | 'right'    | 'nonassoc' | 'generate'  | 'assert'  | 'empty'
  | 'nonempty' | 'global'   | 'explicit' | 'lookahead' | 'param'   | 'flag'
  | 'no-eoi'
  | 'soft'     | 'class'    | 'interface' | 'void'    | 'space'
  | 'layout'   | 'language' | 'lalr'     | 'lexer'     | 'parser'

  # KW
  | [KW] ('true' | 'false' | 'separator' | 'as' | 'import' | 'set')
;

integer_literal ::=
    icon ;

string_literal ::=
    scon ;

boolean_literal ::=
    'true'
  | 'false'
;

literal interface ::=
    string_literal
  | integer_literal
  | boolean_literal
;

pattern ::=
    regexp ;

@noast
qualified_name ::=
    identifier
  | qualified_name '.' identifier<+KW>
;

name ::=
    qualified_name ;

command ::=
    code ;

syntax_problem ::=
    error ;

%input input, expression;

input ::=
    header imports=import_* options=option* lexer=lexer_section parser=parser_section? ;

header ::=
    'language' name=name ('(' target=name ')')? ';' ;

@noast
lexer_section ::=
    '::' 'lexer' lexer_parts ;

@noast
parser_section ::=
    '::' 'parser' grammar_parts ;

import_ ::=
    'import' alias=identifier? path=string_literal ';' ;

option interface ::=
    key=identifier '=' value=expression        {~KeyValue}
  | syntax_problem
;

symref<flag Args> ::=
    [Args]  name=identifier args=symref_args?
  | [!Args] name=identifier
;

rawType class ::=
	  code ;

@noast
lexer_parts ::=
    lexer_part
  | lexer_parts lexer_part<+OrSyntaxError>
;

lexer_part<OrSyntaxError> interface ::=
    state_selector
  | named_pattern
  | lexeme
  | lexer_directive
  | [OrSyntaxError] syntax_problem
;

named_pattern ::=
    name=identifier '=' pattern ;

lexeme ::=
    name=identifier rawTypeopt ':'
          (pattern transition=lexeme_transitionopt priority=integer_literalopt attrs=lexeme_attrsopt commandopt)? ;

lexeme_transition ::=
    '=>' stateref ;

lexeme_attrs ::=
    '(' lexeme_attribute ')' ;

lexeme_attribute ::=
    'soft'
  | 'class'
  | 'space'
  | 'layout'
;

lexer_directive returns lexer_part ::=
    '%' 'brackets' opening=symref<~Args> closing=symref<~Args> ';'    {~directiveBrackets}
;

state_selector ::=
    '[' states=(lexer_state separator ',')+ ']' ;

stateref ::=
    name=identifier ;

lexer_state ::=
    name=identifier ('=>' defaultTransition=stateref)? ;

grammar_parts ::=
    grammar_part
  | grammar_parts grammar_part<+OrSyntaxError>
;

grammar_part<OrSyntaxError> interface ::=
    nonterm
  | template_param
  | directive
  | [OrSyntaxError] syntax_problem
;

nonterm ::=
    annotations? name=identifier params=nonterm_params? type=nonterm_type? '::=' rules ';' ;

nonterm_type interface ::=
    'returns' reference=symref<~Args>                      {~subType}
  | 'interface'                                            {~interfaceType}
  | 'void'                                                 {~voidType}
  | rawType
;

assoc ::=
    'left'
  | 'right'
  | 'nonassoc'
;

param_modifier ::=
    'explicit'
  | 'global'
  | 'lookahead'
;

template_param returns grammar_part ::=
    '%' modifier=param_modifier? param_type name=identifier ('=' param_value)? ';'
;

directive returns grammar_part ::=
    '%' assoc symbols=references ';'                                 {~directivePrio}
  | '%' 'input' inputRefs=(inputref separator ',')+ ';'              {~directiveInput}
  | '%' 'assert' ('empty' | 'nonempty') rhsSet ';'                   {~directiveAssert}
  | '%' 'generate' name=identifier '=' rhsSet ';'                    {~directiveSet}
;

inputref ::=
    reference=symref<~Args> 'no-eoi'? ;

references ::=
    symref<~Args>
  | references symref<~Args>
;

references_cs ::=
    symref<~Args>
  | references_cs ',' symref<~Args>
;

@noast
rules ::=
    rule0
  | rules '|' rule0
;

rule0 interface ::=
    predicate? rhsParts? ruleAction? rhsSuffixopt       {~rule}
  | syntax_problem
;

predicate ::=
    '[' predicate_expression ']' ;

rhsSuffix ::=
    '%' 'prec' symref<~Args>
  | '%' 'shift' symref<~Args>
;

ruleAction ::=
    '{~' action=identifier parameter=string_literal? '}' ;

@noast
rhsParts ::=
    rhsPart
  | rhsParts rhsPart<+OrSyntaxError>
;

rhsPart<OrSyntaxError> interface ::=
    rhsAnnotated
  | command
  | [OrSyntaxError] syntax_problem
;

rhsAnnotated returns rhsPart ::=
    @noast rhsAssignment
  | annotations inner=rhsAssignment
;

rhsAssignment returns rhsPart ::=
    @noast rhsOptional
  | id=identifier '=' inner=rhsOptional
  | id=identifier '+=' inner=rhsOptional     {~rhsPlusAssignment}
;

rhsOptional returns rhsPart ::=
    @noast rhsCast
  | inner=rhsCast '?'
;

rhsCast returns rhsPart ::=
    @noast rhsPrimary
  | inner=rhsPrimary 'as' target=symref<+Args>
;

listSeparator ::=
    'separator' separator_=references ;

rhsPrimary returns rhsPart ::=
    reference=symref<+Args>                           {~rhsSymbol}
  | '(' rules ')'                                     {~rhsNested}
  | '(' ruleParts=rhsParts listSeparator ')' '+'      {~rhsPlusList}
  | '(' ruleParts=rhsParts listSeparator ')' '*'      {~rhsStarList}
  | inner=rhsPrimary '+'                              {~rhsQuantifier}
  | inner=rhsPrimary '*'                              {~rhsQuantifier}
  | '$' '(' rules ')'                                 {~rhsIgnored}
  | rhsSet
;

rhsSet ::=
    'set' '(' expr=setExpression ')' ;

setPrimary returns setExpression ::=
    operator=identifier? symbol=symref<+Args>    {~setSymbol}
  | '(' inner=setExpression ')'                  {~setCompound}
  | '~' inner=setPrimary                         {~setComplement}
;

%left '|';
%left '&';

setExpression interface ::=
    @noast setPrimary
  | left=setExpression '|' right=setExpression   {~setOr}
  | left=setExpression '&' right=setExpression   {~setAnd}
;

annotations ::=
    annotation+ ;

annotation interface ::=
    '@' name=identifier ('=' expression)?    {~annotationImpl}
  | '@' syntax_problem
;

/* Nonterminal parameters */

nonterm_params ::=
    '<' list=(nonterm_param separator ',')+ '>' ;

nonterm_param interface ::=
    param_ref
  | param_type=identifier name=identifier ('=' param_value)?     {~inlineParameter}
;

param_ref ::=
    identifier ;

symref_args ::=
    '<' arg_list=(argument separator ',')* '>' ;

argument interface  ::=
    name=param_ref (':' val=param_value)?        {~argumentImpl}
  | '+' name=param_ref                           {~argumentTrue}
  | '~' name=param_ref                           {~argumentFalse}
;

param_type ::=
    'flag'
  | 'param'
;

param_value interface ::=
    literal
  | param_ref
;

predicate_primary returns predicate_expression ::=
    @noast param_ref
  | '!' param_ref                 {~predicateNot}
  | param_ref '==' literal        {~predicateEq}
  | param_ref '!=' literal        {~predicateNotEq}
;

%left '||';
%left '&&';

predicate_expression interface ::=
    predicate_primary
  | left=predicate_expression '&&' right=predicate_expression        {~predicateAnd}
  | left=predicate_expression '||' right=predicate_expression        {~predicateOr}
;

expression interface ::=
    literal
  | symref<+Args>
  | '[' (expression separator ',')* ']'                              {~array}
  | syntax_problem
;

%%

${template go_lexer.stateVars}
	inStatesSelector bool
${end}

${template go_lexer.initStateVars-}
	l.inStatesSelector = false
${end}

${template go_lexer.onBeforeNext-}
	lastTokenLine := l.tokenLine
${end}

${template go_lexer.onAfterNext-}
	switch token {
	case LT:
		l.inStatesSelector = (lastTokenLine != l.tokenLine) || l.State == StateAfterColonOrEq
		l.State = StateInitial
	case GT:
		if l.inStatesSelector {
			l.State = StateAfterGT
			l.inStatesSelector = false
		} else {
			l.State = StateInitial
		}
	case ASSIGN, COLON:
		l.State = StateAfterColonOrEq
	default:
		l.State = StateInitial
	}
${end}
