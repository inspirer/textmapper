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
genast = true

:: lexer

[initial, afterAt, afterAtID]

reClass = /\[([^\n\r\]\\]|\\.)*\]/
reFirst = /[^\n\r\*\[\\\/]|\\.|{reClass}/
reChar = /{reFirst}|\*/

regexp(string): /\/{reFirst}{reChar}*\//  { text := l.Text(); $$ = text[1:len(text)-2] }
scon(string):   /"([^\n\\"]|\\.)*"/       { text := l.Text(); $$ = text[1:len(text)-2] }
icon(int):      /-?[0-9]+/                { $$, _ = "strconv".ParseInt(l.Text(), 10, 64) }

eoi:           /%%.*(\r?\n)?/
_skip:         /[\n\r\t ]+/               (space)
_skip_comment:  /#.*(\r?\n)?/             (space)

commentChars = /([^*]|\*+[^*\/])*\**/
_skip_multiline: /\/\*{commentChars}\*\// (space)


'%':     /%/
'::=':   /::=/
'::':    /::/
'|':     /\|/
'||':    /\|\|/
'=':     /=/
'==':    /==/
'!=':    /!=/
'=>':    /=>/
';':     /;/
'.':     /\./
',':     /,/
':':     /:/
'[':     /\[/
']':     /\]/
'(':     /\(/
# TODO overlaps with ID '->':  /->/
')':     /\)/
'{~':    /\{~/
'}':     /\}/
'<':     /</
'>':     />/
'*':     /\*/
'+':     /+/
'+=':    /+=/
'?':     /?/
'!':     /!/
'~':     /~/
'&':     /&/
'&&':    /&&/
'$':     /$/
'@':     /@/

error:

[initial, afterAt, afterAtID]

ID(string): /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)  { $$ = l.Text(); }

'true':  /true/
'false': /false/
'new':   /new/
'separator': /separator/
'as': /as/
'import': /import/
'set': /set/

'brackets': /brackets/    (soft)

'inline': /inline/        (soft)

'prec':  /prec/           (soft)
'shift': /shift/          (soft)

'returns': /returns/      (soft)

'input': /input/          (soft)
'left':  /left/           (soft)
'right': /right/          (soft)
'nonassoc': /nonassoc/    (soft)

'generate': /generate/    (soft)
'assert': /assert/        (soft)
'empty': /empty/          (soft)
'nonempty': /nonempty/    (soft)

'global': /global/        (soft)
'explicit': /explicit/    (soft)
'lookahead': /lookahead/  (soft)
'param': /param/          (soft)
'flag': /flag/            (soft)

'no-eoi': /no-eoi/        (soft)

'soft': /soft/            (soft)
'class': /class/          (soft)
'interface': /interface/  (soft)
'void': /void/            (soft)
'space': /space/          (soft)
'layout': /layout/        (soft)
'language': /language/    (soft)
'lalr': /lalr/            (soft)

'lexer': /lexer/          (soft)
'parser': /parser/        (soft)

# reserved

'reduce': /reduce/

[initial, afterAt]

code:   /\{/

[afterAtID]
'{':    /\{/


:: parser

%input input, expression;

input ::=
    header imports=import_* options=option* lexer=lexer_section parser=parser_section? ;

header ::=
    'language' name ('(' target=name ')')? parsing_algorithmopt ';' ;

lexer_section ::=
    '::' 'lexer' @pass lexer_parts ;

parser_section ::=
    '::' 'parser' @pass grammar_parts ;

parsing_algorithm ::=
    'lalr' '(' la=icon ')' ;

import_ ::=
    'import' alias=ID? file=scon ';' ;

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

type (string) ::=
    '(' scon ')'                       { $$ = $scon; }
  | '(' type_part_list ')'             { $$ = "TODO" }
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
    'soft'
  | 'class'
  | 'space'
  | 'layout'
;

lexer_directive returns lexer_part ::=
    '%' 'brackets' opening=symref_noargs closing=symref_noargs ';'   {~directiveBrackets}
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
    'returns' reference=symref_noargs                                {~nontermTypeAST}
  | inline='inline'? kind='class' name=identifieropt implementsopt   {~nontermTypeHint}
  | kind='interface' name=identifieropt implementsopt                {~nontermTypeHint}
  | kind='void'                                                      {~nontermTypeHint}
  | typeText=type                                                    {~nontermTypeRaw}
;

implements ::=
    ':' @pass references_cs ;

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
  | '%' 'assert' (kind='empty' | kind='nonempty') rhsSet ';'         {~directiveAssert}
  | '%' 'generate' name=ID '=' rhsSet ';'                            {~directiveSet}
;

inputref ::=
    reference=symref_noargs noeoi='no-eoi'? ;

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
    '%' kind='prec' symref=symref_noargs
  | '%' kind='shift' symref=symref_noargs
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
;

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
  | inner=rhsCast quantifier='?'        {~rhsQuantifier}
;

rhsCast returns rhsPart ::=
    rhsClass
  | inner=rhsClass 'as' target=symref
  | inner=rhsClass 'as' literal         {~rhsAsLiteral}
;

rhsUnordered returns rhsPart ::=
    left=rhsPart '&' right=rhsPart
;

rhsClass returns rhsPart ::=
    rhsPrimary
  | identifier ':' inner=rhsPrimary
;

rhsPrimary returns rhsPart ::=
    reference=symref                                               {~rhsSymbol}
  | '(' rules ')'                                                  {~rhsNested}
  | '(' ruleParts=rhsParts 'separator' separator_=references ')' atLeastOne='+' as true     {~rhsList}
  | '(' ruleParts=rhsParts 'separator' separator_=references ')' atLeastOne='*' as false    {~rhsList}
  | inner=rhsPrimary quantifier='*'                                {~rhsQuantifier}
  | inner=rhsPrimary quantifier='+'                                {~rhsQuantifier}
  | '$' '(' rules ')'                                              {~rhsIgnored}
  | rhsSet
;

rhsSet returns rhsPart ::=
    'set' '(' expr=setExpression ')'
;

setPrimary returns setExpression ::=
    operator=ID? symbol=symref          {~setSymbol}
  | '(' inner=setExpression ')'         {~setCompound}
  | '~' inner=setPrimary                {~setComplement}
;

setExpression interface ::=
    setPrimary
  | left=setExpression kind='|' right=setExpression     {~setBinary}
  | left=setExpression kind='&' right=setExpression     {~setBinary}
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
    'flag'
  | 'param'
;

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
  | 'new' className=name '(' entries=(map_entry separator ',')* ')'     {~instance}
  | '[' content=(expression separator ',')* ']'                         {~array}
  | syntax_problem
;

map_entry ::=
    name=ID ':' value=expression ;

literal ::=
    value=scon
  | value=icon
  | value='true' as true
  | value='false' as false
;

name class ::=
    qualified_id ;

qualified_id (string) ::=
    ID                              { $$ = $0; }
  | qualified_id '.' ID             { $$ = $qualified_id + "." + $ID; }
;

command class ::=
    code ;

syntax_problem class : lexer_part, grammar_part, rhsPart ::=
    error ;
