#  GNU Bison 3.0.2 input files grammar.

#  Copyright 2002-2017 Evgeny Gryaznov
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

language bison(java);

prefix = "Bison"
package = "org.textmapper.tool.importer"
breaks = true
gentree = true
genmain = true
genast = false
positions = "line,offset"
endpositions = "offset"
defaultExtension = "y"
genCopyright = true

:: lexer

[initial]

#####################
# Common definitions.

letter = /[a-zA-Z\._]/
hex = /[0-9a-fA-F]/

escape = /\\([abfnrtv'"?\\]|[0-7]{1,3}|x{hex}+|(u|U{hex}{4}){hex}{4})/
splice = /(\\[ \f\t\v]*\r?\n)*/

#####################
# Main Bison lexemes.

# ID which is followed by a colon turns into ID_COLON.
ID_COLON:
ID: 		/{letter}({letter}|[0-9\-])*/ 		{ if (lookaheadColon()) $symbol = Tokens.ID_COLON; }

# Colons are detected by the ID rule, so we can ignore them here.
skip:    /:/  (space)
	{
		if (token.offset != foundColonOffset)
			reporter.error("Unexpected colon", token.line, token.offset, token.endoffset);
	}

INT: /[0-9]+/
INT: /0[xX]{hex}+/

CHAR: /'([^'\r\n\\]|{escape})'/
STRING: /"([^"\r\n\\]|{escape})*"/

'<*>': /<\*>/
'<>': /<>/
'%%': /%%/   { if (++sectionCounter == 2) $symbol = Tokens.eoi; }

'|': /\|/
';':  /;/
'[': /\[/
']': /\]/

###################
# Whitespaces

skip: /[\r\n\t\f\v\x20]+/	(space)
skip_comment: /\/\/[^\r\n]*/ (space)
skip_ml_comment: /\/\*([^*]|\*+[^\/*])*\*+\// (space)

###################
# Bison directives.

'%token': /%token/
'%nterm': /%nterm/

'%type': /%type/
'%destructor': /%destructor/
'%printer': /%printer/

'%left': /%left/
'%right': /%right/
'%nonassoc': /%nonassoc/
'%precedence': /%precedence/

'%prec': /%prec/
'%dprec': /%dprec/
'%merge': /%merge/

'%code': /%code/
'%default-prec': /%default-prec/
'%define': /%define/
'%defines': /%defines/
'%empty': /%empty/
'%error-verbose': /%error-verbose/
'%expect': /%expect/
'%expect-rr': /%expect-rr/
'%<flag>': /%<flag>/
'%file-prefix': /%file-prefix/
'%glr-parser': /%glr-parser/
'%initial-action': /%initial-action/
'%language': /%language/
'%name-prefix': /%name-prefix/
'%no-default-prec': /%no-default-prec/
'%no-lines': /%no-lines/
'%nondeterministic-parser': /%nondeterministic-parser/
'%output': /%output/
'%param': /%param/
'%require': /%require/
'%skeleton': /%skeleton/
'%start': /%start/
'%token-table': /%token-table/
'%union': /%union/
'%verbose': /%verbose/
'%yacc': /%yacc/


###################
# Complex tokens.

skip: /{/ 					(space) { state = States.bracedCode; nesting = 0; lexemeStart = token.offset; }
skip: /%\?[ \f\r\n\t\v]*\{/	(space) { state = States.predicate; nesting = 0; lexemeStart = token.offset; }
skip: /%{/					(space) { state = States.prologue; nesting = 0; lexemeStart = token.offset; }
skip: /</					(space) { state = States.tag; nesting = 0; lexemeStart = token.offset; }

[bracedCode]
'{...}' {String}:  /}/
	{
		nesting--;
		if (nesting < 0) {
			setState(States.initial);
			token.offset = lexemeStart;
			token.value = ""; // TODO
		} else {
			spaceToken = true;
		}
	}

[predicate]
'%?{...}':  /}/    { nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }

[prologue]
'%{...%}':   /%}/  { state = States.initial; token.offset = lexemeStart; }

[tag]
tag_any: /([^<>]|->)+/  (space)
tag_inc_nesting: /</  (space)  { nesting++; }
TAG: />/ 		{ nesting--; if (nesting < 0) { setState(States.initial); token.offset = lexemeStart; } else { spaceToken = true; } }

[bracedCode, prologue, epilogue, predicate]
code_char: /'([^'\n\\]|{escape})*'/  (space)
code_string: /"([^"\n\\]|{escape})*"/ (space)
code_comment: /\/{splice}\/[^\r\n]*/ (space)                                # TODO more {splice}
code_ml_comment: /\/{splice}\*([^*]|\*+[^\/*])*\*+\// (space)				# TODO more {splice}
code_any: /.|\n/ -1 (space)

[bracedCode, predicate]
code_inc_nesting: /{|<{splice}%/ (space) 	{ nesting++; }
code_dec_nesting: /%{splice}>/ (space) 		{ nesting--; }

# Tokenize '<<%' correctly (as '<<' '%')
code_lessless: /<{splice}</ (space)

:: parser

# Outline of a Bison Grammar:
#	%{
#	  Prologue
#	%}
#
#	Bison declarations
#
#	%%
#	Grammar rules
#	%%					<- We treat the second %% as EOI.
#
#	Epilogue

input ::=
	  prologue_declaration*
	  '%%'
	  grammar_part+
;

prologue_declaration ::=
	  grammar_declaration
	| prologue_directive
	| '%{...%}'
;

prologue_directive ::=
	  '%<flag>'
	| '%define' variable valueopt
	| '%defines'
	| '%defines' STRING
	| '%error-verbose'
	| '%expect' INT
	| '%expect-rr' INT
	| '%file-prefix' STRING
	| '%glr-parser'
	| '%initial-action' '{...}'
	| '%language' STRING
	| '%name-prefix' STRING
	| '%no-lines'
	| '%nondeterministic-parser'
	| '%output' STRING
	| '%param' '{...}'+
	| '%require' STRING
	| '%skeleton' STRING
	| '%token-table'
	| '%verbose'
	| '%yacc'
	| ';'
;

grammar_declaration ::=
	  prec_declaration
	| symbol_declaration
	| code_props_type '{...}' symbol_or_tag+
	| '%start' symbol
	| '%default-prec'
	| '%no-default-prec'
	| '%code' '{...}'
	| '%code' ID '{...}'
	| '%union' ID? '{...}'
;

code_props_type ::=
	  '%destructor'
	| '%printer'
;

symbol_declaration ::=
	  '%nterm' symbol_def+
	| '%token' symbol_def+
	| '%type' TAG symbol+
;

prec_declaration ::=
	  prec_directive tag_op symbol_prec+ ;

prec_directive ::=
	  '%left'
	| '%right'
	| '%nonassoc'
	| '%precedence'
;

tag_op ::=
	  TAG? ;

symbol_prec ::=
	  symbol INT? ;

symbol_or_tag ::=
	  symbol
	| tag_nt
;

tag_nt ::=
	  TAG
	| '<*>'
	| '<>'
;

symbol_def ::=
	  TAG
	| ID INT? STRING?
	| CHAR INT? STRING?
;

grammar_part ::=
	  nonterm_rules
	| grammar_declaration ';'
;

nonterm_rules ::=
	  ID_COLON named_ref_op rules ;

rules ::=
	  rhsPart*
	| rules '|' rhsPart*
	| rules ';'
;

rhsPart ::=
	  symbol named_ref_op
	| '{...}' named_ref_op
	| '%?{...}'
	| '%empty'
	| '%prec' symbol
	| '%dprec' INT
	| '%merge' TAG
;

named_ref_op ::=
	  ('[' ID ']')?
;

variable ::=
	  ID | STRING ;

value ::=
	  ID
	| STRING
	| '{...}'
;

symbol ::=
	  ID
	| CHAR
	| STRING
;

%%

${template java_lexer.lexercode}
private int nesting = 0;
private int lexemeStart = -1;
private int foundColonOffset = -1;
private int sectionCounter = 0;

private boolean lookaheadColon() throws IOException {
	int offset = 0;
	// TODO handle "aa [ bb ] :"
	while (charAt(offset) == ' ') offset++;
	if (charAt(offset) == ':') {
		foundColonOffset = currOffset + offset;
		return true;
	}
	return false;
}
${end}
