#  syntax: lalr1 generator source grammar

#  Copyright 2002-2022 Evgeny Gryaznov
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

%s initial, afterID, afterColonOrEq, afterGT;

reClass = /\[([^\n\r\]\\]|\\.)*\]/
reFirst = /[^\n\r\*\[\\\/]|\\.|{reClass}/
reChar = /{reFirst}|\*/

scon {String}:  /"([^\n\\"]|\\.)*"/    { $$ = unescape(tokenText(), 1, tokenSize()-1); }
icon {Integer}: /-?[0-9]+/             { $$ = Integer.parseInt(tokenText()); }

eoi:           /%%.*(\r?\n)?/          { templatesStart = token.endoffset; }
_skip:         /[\n\r\t ]+/    (space)
_skip_comment: /#.*(\r?\n)?/           { spaceToken = skipComments; }

commentChars = /([^*]|\*+[^*\/])*\**/
_skip_multiline: /\/\*{commentChars}\*\// (space)


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

ID: /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/  (class)

'as':        /as/
'extend':    /extend/
'false':     /false/
'implements':/implements/
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
'flag':      /flag/
'generate':  /generate/
'global':    /global/
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
'returns':   /returns/
'right':     /right/
's':         /s/
'shift':     /shift/
'space':     /space/
'void':      /void/
'x':         /x/

<initial, afterID, afterColonOrEq>
code:   /\{/                                { skipAction(); token.endoffset = getOffset(); }

<afterGT>
'{':  /\{/

<afterColonOrEq>
regexp {String}: /\/{reFirst}{reChar}*\//   { $$ = tokenText().substring(1, tokenSize()-1); }

<initial, afterID, afterGT>
'/':    /\//

:: parser

identifier class :
    ID

  # Soft keywords
  | 'brackets' | 'inline'    | 'prec'     | 'shift'     | 'returns' | 'input'
  | 'left'     | 'right'     | 'nonassoc' | 'generate'  | 'assert'  | 'empty'
  | 'nonempty' | 'global'    | 'explicit' | 'lookahead' | 'param'   | 'flag'
  | 'no-eoi'   | 's'         | 'x'        | 'expect'    | 'expect-rr'
  | 'class'    | 'interface' | 'void'     | 'space'
  | 'layout'   | 'language'  | 'lalr'     | 'lexer'      | 'parser'
;

literal :
    value=scon
  | value=icon
  | value='true' as true
  | value='false' as false
;

pattern class :
    regexp ;

qualified_id {String} :
    identifier                         { $$ = $identifier.getText(); }
  | qualified_id '.' identifier        { $$ = $qualified_id + "." + $identifier.getText(); }
;

name class :
    qualified_id ;

command class :
    code ;

syntax_problem class implements lexer_part, grammar_part, rhsPart :
    error ;

%input input, expression;

input :
    header imports=import_* options=option* lexer=lexer_section parser=parser_section? ;

header :
    'language' name ('(' target=name ')')? ';' ;

lexer_section :
    '::' 'lexer' @pass lexer_parts ;

parser_section :
    '::' 'parser' @pass grammar_parts ;

import_ :
    'import' alias=identifier? file=scon ';' ;

option :
    key=identifier '=' value=expression
  | syntax_problem
;

symref class :
    name=identifier args=symref_args? ;

symref_noargs returns symref :
    name=identifier ;

rawType class :
    code ;

lexer_parts :
    lexer_part
  | lexer_parts lexer_part
  | lexer_parts syntax_problem
;

lexer_part :
    named_pattern
  | lexeme
  | states_clause
  | brackets_directive
  | start_conditions_scope
;

named_pattern :
    name=identifier '=' pattern ;

start_conditions_scope :
    start_conditions '{' lexer_parts '}' ;

start_conditions :
    '<' '*'  '>'
  | '<' (stateref separator ',')+ '>'
;

lexeme :
    start_conditions? name=identifier rawTypeopt reportClause? ':'
        (pattern priority=iconopt attrs=lexeme_attrsopt commandopt | attrs=lexeme_attrs)? ;

lexeme_attrs :
    '(' kind=lexeme_attribute ')' ;

lexeme_attribute :
    'class'
  | 'space'
  | 'layout'
;

brackets_directive returns lexer_part :
    '%' 'brackets' opening=symref_noargs closing=symref_noargs ';' ;

states_clause returns lexer_part :
    '%' exclusive='s' as false states=(lexer_state separator ',')+ ';'
  | '%' exclusive='x' as true states=(lexer_state separator ',')+ ';'
;

stateref class :
    name=identifier ;

lexer_state :
    name=identifier ;

grammar_parts :
    grammar_part
  | grammar_parts grammar_part
  | grammar_parts syntax_problem
;

grammar_part :
    nonterm
  | template_param
  | directive
;

nonterm :
    annotations? 'extend'? name=identifier params=nonterm_params? type=nonterm_type?
          defaultAction=reportClause? ':' rules ';' ;

nonterm_type interface :
    'returns' reference=symref_noargs                                     -> nontermTypeAST
  | inline='inline'? kind='class' implements_clauseopt                    -> nontermTypeHint
  | kind='interface'                                                      -> nontermTypeHint
  | kind='void'                                                           -> nontermTypeHint
  | rawType
;

implements_clause :
    'implements' @pass references_cs ;

assoc :
    'left'
  | 'right'
  | 'nonassoc'
;

param_modifier :
    'explicit'
  | 'global'
  | 'lookahead'
;

template_param returns grammar_part :
    '%' modifier=param_modifier? param_type name=identifier ('=' param_value)? ';'
;

directive returns grammar_part :
    '%' assoc symbols=references ';'                           -> directivePrio
  | '%' 'input' inputRefs=(inputref separator ',')+ ';'        -> directiveInput
  | '%' 'interface' ids=(identifier separator ',')+ ';'        -> directiveInterface
  | '%' 'assert' (kind='empty' | kind='nonempty') rhsSet ';'   -> directiveAssert
  | '%' 'generate' name=identifier '=' rhsSet ';'              -> directiveSet
  | '%' 'expect' icon ';'                                      -> directiveExpect
  | '%' 'expect-rr' icon ';'                                   -> directiveExpectRR
;

inputref :
    reference=symref_noargs noeoi='no-eoi'? ;

references :
    symref_noargs
  | references symref_noargs
;

references_cs :
    symref_noargs
  | references_cs ',' symref_noargs
;

rules :
    (rule0 separator '|')+ ;

rule0 :
    predicate? list=rhsParts? suffix=rhsSuffixopt action=reportClause?
  | error=syntax_problem
;

predicate :
    '[' @pass predicate_expression ']' ;

rhsSuffix :
    '%' kind='prec' symref=symref_noargs
  | '%' kind='shift' symref=symref_noargs
;

reportClause :
    '->' action=identifier ('/' flags=(identifier separator ',')+)? reportAs? ;

reportAs:
    'as' identifier ;

rhsParts :
    rhsPart
  | rhsParts rhsPart
  | rhsParts syntax_problem
;

rhsPart :
    rhsAnnotated
  | command
  | rhsStateMarker
  | rhsLookahead
;

rhsLookahead :
    '(?=' predicates=(lookahead_predicate separator '&')+ ')' ;

lookahead_predicate :
    negate='!'? symref_noargs ;

rhsStateMarker :
    '.' name=identifier ;

rhsAnnotated returns rhsPart :
    rhsAssignment
  | annotations inner=rhsAssignment
;

rhsAssignment returns rhsPart :
    rhsOptional
  | id=identifier ('=' | addition='+=') inner=rhsOptional
;

rhsOptional returns rhsPart :
    rhsCast
  | inner=rhsCast quantifier='?'            -> rhsQuantifier
;

rhsCast returns rhsPart :
    rhsPrimary
  | inner=rhsPrimary 'as' target=symref
  | inner=rhsPrimary 'as' literal              -> rhsAsLiteral
;

rhsPrimary returns rhsPart :
    reference=symref                        -> rhsSymbol
  | '(' rules ')'                           -> rhsNested
  | '(' ruleParts=rhsParts 'separator' separator_=references ')' atLeastOne='+' as true   -> rhsList
  | '(' ruleParts=rhsParts 'separator' separator_=references ')' atLeastOne='*' as false  -> rhsList
  | inner=rhsPrimary quantifier='*'         -> rhsQuantifier
  | inner=rhsPrimary quantifier='+'         -> rhsQuantifier
  | '$' '(' rules ')'                       -> rhsIgnored
  | rhsSet
;

rhsSet returns rhsPart :
    'set' '(' expr=setExpression ')' ;

setPrimary returns setExpression :
    operator=identifier? symbol=symref              -> setSymbol
  | '(' inner=setExpression ')'             -> setCompound
  | '~' inner=setPrimary                    -> setComplement
;

%left '|';
%left '&';

setExpression interface :
    setPrimary
  | left=setExpression kind='|' right=setExpression   -> setBinary
  | left=setExpression kind='&' right=setExpression   -> setBinary
;

annotations class :
    annotations=annotation+ ;

annotation :
    '@' name=identifier ('=' expression)?
  | '@' syntax_problem
;

##### Nonterminal parameters

nonterm_params :
    '<' list=(nonterm_param separator ',')+ '>' ;

nonterm_param interface :
    param_ref
  | param_type=identifier name=identifier ('=' param_value)?  -> inlineParameter
;

param_ref :
    ref=identifier ;

symref_args :
    '<' arg_list=(argument separator ',')* '>' ;

argument :
    name=param_ref ':' val=param_value
  | (bool='+'|bool='~')? name=param_ref
;

param_type :
    'flag'
  | 'param'
;

param_value :
    literal
  | symref_noargs
;

predicate_primary returns predicate_expression :
    negated='!'? param_ref                            -> boolPredicate
  | param_ref (kind='==' | kind='!=') literal         -> comparePredicate
;

%left '||';
%left '&&';

predicate_expression interface :
    predicate_primary
  | left=predicate_expression kind='&&' right=predicate_expression   -> predicateBinary
  | left=predicate_expression kind='||' right=predicate_expression   -> predicateBinary
;

expression :
    literal
  | symref
  | '[' content=(expression separator ',')+? ','? ']'                 -> array
  | syntax_problem
;

##################################################################################

%%

${template java.imports-}
${call base-}
import java.util.List;
import java.util.ArrayList;
import org.textmapper.tool.parser.ast.*;
${end}

${template java_lexer.onReset-}
inStatesSelector = false;
afterColonColon = false;
${end}

${template java_lexer.lexercode}
protected boolean inStatesSelector = false;
protected boolean afterColonColon = false;
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
${if inp.target.id == 'input1'-}
if (result != null) {
	result.setTemplatesStart(lexer.getTemplatesStart());
}
${end-}
${end}

${query java_ast.astSource() = 'source'}
${query java_ast.astNodeExtends() = ' extends org.textmapper.lapg.api.@TextSourceElement'}

${template java_ast.ast_class_fields(cl)-}
${if cl.name == 'Input1'-}
	private int templatesStart = -1;
${end-}
${end}

${template java_ast.ast_class_methods(cl)-}
${if cl.name == 'Input1'}
	public int getTemplatesStart() {
		return templatesStart;
	}

	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}
${end-}
${end}

${template java_lexer.onAfterNext-}
switch (token.symbol) {
case Tokens.Lt:
	inStatesSelector = this.state == States.initial || this.state == States.afterColonOrEq;
	this.state = States.initial;
	break;
case Tokens.Gt:
	this.state = inStatesSelector ? States.afterGT : States.initial;
	inStatesSelector = false;
	break;
case Tokens.Assign:
case Tokens.Colon:
	this.state = States.afterColonOrEq;
	break;
case Tokens.ID:
case Tokens.left:
case Tokens.right:
case Tokens.nonassoc:
case Tokens.generate:
case Tokens._assert:
case Tokens.empty:
case Tokens.brackets:
case Tokens.inline:
case Tokens.prec:
case Tokens.shift:
case Tokens.returns:
case Tokens.input:
case Tokens.nonempty:
case Tokens.global:
case Tokens.explicit:
case Tokens.lookahead:
case Tokens.param:
case Tokens.flag:
case Tokens.noMinuseoi:
case Tokens.char_s:
case Tokens.char_x:
case Tokens._class:
case Tokens._interface:
case Tokens._void:
case Tokens.space:
case Tokens.layout:
case Tokens.language:
case Tokens.lalr:
  this.state = States.afterID;
  break;
case Tokens.lexer:
case Tokens.parser:
  this.state = afterColonColon ? States.initial : States.afterID;
  break;
case Tokens._skip:
case Tokens._skip_comment:
case Tokens._skip_multiline:
  // Note: these do not affect the '::' tracking.
	return token;
default:
	this.state = States.initial;
}
this.afterColonColon = (token.symbol == Tokens.ColonColon);
${end}
