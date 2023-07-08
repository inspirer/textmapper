#  syntax: lalr1 generator source grammar

#  Copyright 2002-2020 Evgeny Gryaznov
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

language tm(go);

lang = "tm"
package = "github.com/inspirer/textmapper/parsers/tm"
eventBased = true
eventFields = true
cancellable = true
eventAST = true
writeBison = true
tokenColumn = true
fileNode = "File"
reportTokens = [invalid_token, multilineComment, comment, templates]

:: lexer

%s initial, afterID, afterColonOrEq, afterGT;

reClass = /\[([^\n\r\]\\]|\\.)*\]/
reFirst = /[^\n\r\*\[\\\/]|\\.|{reClass}/
reChar = /{reFirst}|\*/

scon:    /"([^\n\\"]|\\.)*"/
icon:    /-?[0-9]+/

templates:  /%%/                             (space)   { l.rewind(len(l.source)) }
whitespace: /[\n\r\t ]+/                     (space)
comment:    /(#|\/\/)[^\r\n]*/               (space)

commentChars = /([^*]|\*+[^*\/])*\**/
multilineComment: /\/\*{commentChars}\*\//   (space)

'%':    /%/
'::':   /::/
'|':    /\|/
'||':   /\|\|/
'=':    /=/
'==':   /==/
'!=':   /!=/
';':    /;/
'.':    /\./
',':    /,/
':':    /:/
'[':    /\[/
']':    /\]/
'(':    /\(/
'(?=': /\(\?=/
# TODO overlaps with ID
'->':   /->/
')':    /\)/
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

'as':        /as/
'false':     /false/
'import':    /import/
'separator': /separator/
'set':       /set/
'true':      /true/

# Soft keywords.

'assert':    /assert/
'brackets':  /brackets/
'class':     /class/
'empty':     /empty/
'expect':    /expect/
'expect-rr': /expect-rr/
'explicit':  /explicit/
'extend':    /extend/
'flag':      /flag/
'generate':  /generate/
'global':    /global/
'inject':    /inject/
'inline':    /inline/
'input':     /input/
'interface': /interface/
'lalr':      /lalr/
'language':  /language/
'layout':    /layout/
'left':      /left/
'lexer':     /lexer/
'lookahead': /lookahead/
'no-eoi':    /no-eoi/
'nonassoc':  /nonassoc/
'nonempty':  /nonempty/
'param':     /param/
'parser':    /parser/
'prec':      /prec/
'right':     /right/
's':         /s/
'shift':     /shift/
'space':     /space/
'x':         /x/

<initial, afterID, afterColonOrEq>
code:   /\{/    /* We skip the rest in a post-processing action. */

<afterGT>
'{':  /\{/

<afterColonOrEq>
regexp: /\/{reFirst}{reChar}*\//

<initial, afterID, afterGT>
'/':    /\//

:: parser

%input file, nonterm;

%flag OrSyntaxError = false;

# Basic nonterminals.

identifier<flag Keywords = false> -> Identifier:
    ID

# Soft keywords
  | 'brackets' | 'inline'    | 'prec'     | 'shift'     | 'input'
  | 'left'     | 'right'     | 'nonassoc' | 'generate'  | 'assert'  | 'empty'
  | 'nonempty' | 'global'    | 'explicit' | 'lookahead' | 'param'   | 'flag'
  | 'no-eoi'   | 's'         | 'x'        | 'expect'    | 'expect-rr'
  | 'class'    | 'interface' | 'space'    | 'extend'    | 'inject'
  | 'layout'   | 'language'  | 'lalr'     | 'lexer'     | 'parser'

  # Keywords
  | [Keywords] ('true' | 'false' | 'separator' | 'as' | 'import' | 'set')
;

integer_literal -> IntegerLiteral:
    icon ;

string_literal -> StringLiteral:
    scon ;

boolean_literal -> BooleanLiteral:
    'true'
  | 'false'
;

%interface Literal;

literal -> Literal:
    string_literal
  | integer_literal
  | boolean_literal
;

pattern -> Pattern:
    regexp ;

command -> Command:
    code ;

syntax_problem -> SyntaxProblem:
    error ;

file -> File:
    header imports=import_* options=option* syntax_problem? lexer=lexer_section? parser=parser_section? ;

header -> Header:
    'language' name=identifier<+Keywords> ('(' target=identifier<+Keywords> ')')? ';' ;

lexer_section -> LexerSection:
    '::' .recoveryScope 'lexer' lexer_parts ;

parser_section -> ParserSection:
    '::' .recoveryScope 'parser' grammar_parts ;

import_ -> Import:
    'import' alias=identifier? path=string_literal ';' ;

option -> Option:
    key=identifier '=' value=expression ;

symref<flag Args> -> Symref:
    [Args]  name=identifier args=args?
  | [!Args] name=identifier
;

rawType -> RawType:
    code ;

lexer_parts:
    lexer_part
  | lexer_parts lexer_part<+OrSyntaxError>
;

%interface LexerPart;

lexer_part<OrSyntaxError> -> LexerPart:
    named_pattern
  | lexeme
  | lexer_directive
  | start_conditions_scope
  | [OrSyntaxError] syntax_problem
;

named_pattern -> NamedPattern:
    name=identifier '=' pattern ;

start_conditions_scope -> StartConditionsScope:
    start_conditions '{' .recoveryScope lexer_parts '}' ;

start_conditions -> StartConditions:
    '<' '*'  '>'
  | '<' (stateref separator ',')+ '>'
;

lexeme -> Lexeme:
    start_conditions? name=identifier rawTypeopt reportClause? ':'
        (pattern priority=integer_literal? attrs=lexeme_attrs? command? | attrs=lexeme_attrs)? ;

lexeme_attrs -> LexemeAttrs:
    '(' lexeme_attribute ')' ;

lexeme_attribute -> LexemeAttribute:
    'class'
  | 'space'
  | 'layout'
;

lexer_directive -> LexerPart:
    '%' 'brackets' opening=symref<~Args> closing=symref<~Args> ';'    -> DirectiveBrackets
  | '%' 's' states=(lexer_state separator ',')+ ';'                   -> InclusiveStartConds
  | '%' 'x' states=(lexer_state separator ',')+ ';'                   -> ExclusiveStartConds
;

stateref -> Stateref:
    name=identifier ;

lexer_state -> LexerState:
    name=identifier ;

grammar_parts:
    grammar_part
  | grammar_parts grammar_part<+OrSyntaxError>
;

%interface GrammarPart;

grammar_part<OrSyntaxError> -> GrammarPart:
    nonterm
  | template_param
  | directive
  | [OrSyntaxError] syntax_problem
;

nonterm -> Nonterm:
    annotations? name=identifier params=nonterm_params? rawType? reportClause? ':' rules ';' 
  | ('extend' -> Extend) name=identifier reportClause? ':' rules ';'
;

assoc -> Assoc:
    'left'
  | 'right'
  | 'nonassoc'
;

param_modifier -> ParamModifier:
    'explicit'
  | 'global'
  | 'lookahead'
;

template_param -> GrammarPart:
    '%' modifier=param_modifier? param_type name=identifier ('=' param_value)? ';' -> TemplateParam
;

directive -> GrammarPart:
    '%' assoc symbols=references ';'                                 -> DirectivePrio
  | '%' 'input' inputRefs=(inputref separator ',')+ ';'              -> DirectiveInput
  | '%' 'interface' ids=(identifier separator ',')+ ';'              -> DirectiveInterface
  | '%' 'assert' ('empty' -> Empty | 'nonempty' -> NonEmpty) rhsSet ';' -> DirectiveAssert
  | '%' 'generate' name=identifier '=' rhsSet ';'                    -> DirectiveSet
  | '%' 'expect' integer_literal ';'                                 -> DirectiveExpect
  | '%' 'expect-rr' integer_literal ';'                              -> DirectiveExpectRR
  | '%' 'inject' symref<~Args> reportClause ';'                      -> DirectiveInject
;

inputref -> Inputref:
    reference=symref<~Args> ('no-eoi' -> NoEoi)? ;

references:
    symref<~Args>
  | references symref<~Args>
;

rules:
    rule0
  | rules '|' rule0
;

%interface Rule0;

rule0 -> Rule0:
    predicate? rhsParts? rhsSuffix? reportClause?       -> Rule
  | syntax_problem
;

predicate -> Predicate:
    '[' predicate_expression ']' ;

rhsSuffix -> RhsSuffix:
    '%' ('prec' -> Name) symref<~Args>
  | '%' ('shift' -> Name) symref<~Args>
;

reportClause -> ReportClause:
    '->' action=identifier ('/' flags=(identifier separator ',')+)? reportAs? ;

reportAs -> ReportAs:
    'as' identifier ;

rhsParts:
    rhsPart
  | rhsParts rhsPart<+OrSyntaxError>
;

%interface RhsPart;

rhsPart<OrSyntaxError> -> RhsPart:
    rhsAnnotated
  | command
  | rhsStateMarker
  | rhsLookahead
  | [OrSyntaxError] syntax_problem
;

rhsLookahead -> RhsLookahead:
    '(?=' predicates=(lookahead_predicate separator '&')+ ')' ;

lookahead_predicate -> LookaheadPredicate:
    ('!' -> Not)? symref<~Args> ;

rhsStateMarker -> StateMarker:
    '.' name=identifier ;

rhsAnnotated -> RhsPart:
    rhsAssignment
  | annotations inner=rhsAssignment  -> RhsAnnotated
;

rhsAssignment -> RhsPart:
    rhsOptional
  | id=identifier '=' inner=rhsOptional      -> RhsAssignment
  | id=identifier '+=' inner=rhsOptional     -> RhsPlusAssignment
;

rhsOptional -> RhsPart:
    rhsCast
  | inner=rhsCast '?'  -> RhsOptional
;

rhsCast -> RhsPart:
    rhsPrimary
  | inner=rhsPrimary 'as' target=symref<+Args> -> RhsCast
  | inner=rhsPrimary 'as' literal              -> RhsAsLiteral   /* TODO remove */
;

listSeparator -> ListSeparator:
    'separator' separator_=references ;

rhsPrimary -> RhsPart:
    reference=symref<+Args>                           -> RhsSymbol
  | '(' .recoveryScope rules ')'                                     -> RhsNested
  | '(' .recoveryScope ruleParts=rhsParts listSeparator ')' '+'      -> RhsPlusList
  | '(' .recoveryScope ruleParts=rhsParts listSeparator ')' '*'      -> RhsStarList
  | inner=rhsPrimary '+'                              -> RhsPlusQuantifier
  | inner=rhsPrimary '*'                              -> RhsStarQuantifier
  | '$' '(' .recoveryScope rules ')'                                 -> RhsIgnored
  | rhsSet
;

rhsSet -> RhsSet:
    'set' '(' .recoveryScope expr=setExpression ')' ;

%interface SetExpression;

setPrimary -> SetExpression:
    operator=identifier? symbol=symref<+Args>    -> SetSymbol
  | '(' inner=setExpression ')'                  -> SetCompound
  | '~' inner=setPrimary                         -> SetComplement
;

%left '|';
%left '&';

setExpression -> SetExpression:
    setPrimary
  | left=setExpression '|' right=setExpression   -> SetOr
  | left=setExpression '&' right=setExpression   -> SetAnd
;

annotations -> Annotations:
    annotation+ ;

%interface Annotation;

annotation -> Annotation:
    '@' name=identifier ('=' expression)?    -> AnnotationImpl
  | '@' syntax_problem
;

/* Nonterminal parameters */

nonterm_params -> NontermParams:
    '<' list=(nonterm_param separator ',')+ '>' ;

%interface NontermParam;

nonterm_param -> NontermParam:
    param_ref
  | param_type=identifier name=identifier ('=' param_value)?     -> InlineParameter
;

param_ref -> ParamRef:
    identifier ;

args -> SymrefArgs:
    '<' arg_list=(argument separator ',')* '>' ;

%interface Argument;

argument -> Argument:
    name=param_ref (':' val=param_value)?        -> ArgumentVal
  | '+' name=param_ref                           -> ArgumentTrue
  | '~' name=param_ref                           -> ArgumentFalse
;

param_type -> ParamType:
    'flag'
  | 'param'
;

%interface ParamValue;

param_value -> ParamValue:
    literal
  | param_ref
;

predicate_primary -> PredicateExpression:
    param_ref
  | '!' param_ref                 -> PredicateNot
  | param_ref '==' literal        -> PredicateEq
  | param_ref '!=' literal        -> PredicateNotEq
;

%left '||';
%left '&&';

%interface PredicateExpression;

predicate_expression -> PredicateExpression:
    predicate_primary
  | left=predicate_expression '&&' right=predicate_expression        -> PredicateAnd
  | left=predicate_expression '||' right=predicate_expression        -> PredicateOr
;

%interface Expression;

expression -> Expression:
    literal
  | symref<+Args>
  | '[' (expression separator ',')+? ','? ']'                         -> Array
  | syntax_problem
;

%%

${template go_lexer.stateVars-}
	inStatesSelector bool
	prev             token.Token
${end}

${template go_lexer.initStateVars-}
	l.inStatesSelector = false
	l.prev = token.UNAVAILABLE
${end}

${template go_lexer.onAfterNext-}
	switch tok {
	case token.LT:
		l.inStatesSelector = l.State == StateInitial || l.State == StateAfterColonOrEq
		l.State = StateInitial
	case token.GT:
		if l.inStatesSelector {
			l.State = StateAfterGT
			l.inStatesSelector = false
		} else {
			l.State = StateInitial
		}
	case token.ID, token.LEFT, token.RIGHT, token.NONASSOC, token.GENERATE,
    token.ASSERT, token.EMPTY, token.BRACKETS, token.INLINE, token.PREC,
    token.SHIFT, token.INPUT, token.NONEMPTY, token.GLOBAL,
    token.EXPLICIT, token.LOOKAHEAD, token.PARAM, token.FLAG, token.CHAR_S,
    token.CHAR_X, token.CLASS, token.INTERFACE, token.SPACE,
		token.LAYOUT, token.LANGUAGE, token.LALR, token.EXTEND:

		l.State = StateAfterID
	case token.LEXER, token.PARSER:
		if l.prev == token.COLONCOLON {
			l.State = StateInitial
		} else {
			l.State = StateAfterID
		}
	case token.ASSIGN, token.COLON:
		l.State = StateAfterColonOrEq
	case token.CODE:
		if !l.skipAction() {
			tok = token.INVALID_TOKEN
		}
		fallthrough
	default:
		l.State = StateInitial
	}
	l.prev = tok
${end}

${template go_types.wrappedTypeExt-}
	SourceRange() "github.com/inspirer/textmapper/status".SourceRange
${end}
