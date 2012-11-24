#  syntax: regular expression grammar

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
prefix = "RegexDef"
package = "org.textmapper.lapg.regex"
breaks = true
gentree = true
positions = "offset"
endpositions = "offset"
genCopyright = true


# 0 - default
# 1 - after character/group - enables qualifiers
# 2 - in character set

[initial, afterChar, inSet]

char(Character): /[^()\[\]\.|\\\/*?+-]/			{ $symbol = current().charAt(0); quantifierReady(); }
escaped(Character): /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/
												{ $symbol = current().charAt(1); quantifierReady(); }
escaped(Character): /\\a/						{ $symbol = (char) 7; quantifierReady(); }
escaped(Character): /\\b/						{ $symbol = '\b'; quantifierReady(); }
escaped(Character): /\\f/						{ $symbol = '\f'; quantifierReady(); }
escaped(Character): /\\n/						{ $symbol = '\n'; quantifierReady(); }
escaped(Character): /\\r/						{ $symbol = '\r'; quantifierReady(); }
escaped(Character): /\\t/						{ $symbol = '\t'; quantifierReady(); }
escaped(Character): /\\v/						{ $symbol = (char) 0xb; quantifierReady(); }
escaped(Character): /\\[0-7][0-7][0-7]/			{ $symbol = RegexUtil.unescapeOct(current().substring(1)); quantifierReady(); }
hx = /[0-9A-Fa-f]/
escaped(Character): /\\[xX]{hx}{hx}/			{ $symbol = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); }
escaped(Character): /\\[uU]{hx}{hx}{hx}{hx}/	{ $symbol = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); }
charclass(String): /\\[wWsSdD]/					{ $symbol = current().substring(1); quantifierReady(); }
charclass(String): /\\p\{\w+\}/					{ $symbol = current().substring(3, current().length() - 1); quantifierReady(); }

'.':  /\./										{ quantifierReady(); }

[afterChar => initial]

'*':  /*/
'+':  /+/
'?':  /?/
quantifier:  /\{[0-9]+(,[0-9]*)?\}/

op_minus:		/\{-\}/
op_union:		/\{+\}/
op_intersect:	/\{&&\}/

[initial, inSet]

char(Character): /[*+?]/						{ $symbol = current().charAt(0); quantifierReady(); }

[initial, afterChar]

'(':  /\(/										{ state = 0; }
'|':  /\|/										{ state = 0; }
')':  /\)/										{ quantifierReady(); }

'(?':	/\(\?[is-]+:/							{ state = 0; }

'[':	/\[/  => inSet
'[^':	/\[^/ => inSet
char(Character):  /-/							{ $symbol = current().charAt(0); quantifierReady(); }

identifier = /[a-zA-Z_][a-zA-Z_\-0-9]*/

expand:			/\{{identifier}\}/	(class)		{ quantifierReady(); }
kw_eoi:			/\{eoi\}/						{ state = 0; }

[inSet]

']':  /\]/										{ state = 0; quantifierReady(); }
'-':  /-/
char(Character):  /[\(\|\)]/					{ $symbol = current().charAt(0); }

# grammar

%input pattern;

pattern (RegexAstPart) ::=
	  partsopt									{ $$ = RegexUtil.emptyIfNull($partsopt, source, ${partsopt.offset}); }
	| left=pattern '|' partsopt					{ $$ = RegexUtil.createOr($left, $partsopt, source, ${partsopt.offset}); }
;

part (RegexAstPart) ::=
	  primitive_part
	| primitive_part '*'						{ $$ = new RegexAstQuantifier($primitive_part, 0, -1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part '+'						{ $$ = new RegexAstQuantifier($primitive_part, 1, -1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part '?'						{ $$ = new RegexAstQuantifier($primitive_part, 0, 1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part quantifier					{ $$ = RegexUtil.createQuantifier($primitive_part, source, ${quantifier.offset}, ${left().endoffset}, reporter); }
;

primitive_part (RegexAstPart) ::=
	  char										{ $$ = new RegexAstChar($char, source, ${left().offset}, ${left().endoffset}); }
	| escaped									{ $$ = new RegexAstChar($escaped, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexAstCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder, reporter, ${left().offset}, ${left().endoffset}), source, ${left().offset}, ${left().endoffset}); }
	| '.'										{ $$ = new RegexAstAny(source, ${left().offset}, ${left().endoffset}); }
	| '(' pattern ')'							{ $$ = RegexUtil.wrap($pattern); }
	| '[' charset ']'							{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, false); }
	| '[^' charset ']'							{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, true); }
	| expand									{ $$ = new RegexAstExpand(source, ${left().offset}, ${left().endoffset}); RegexUtil.checkExpand((RegexAstExpand) $$, reporter); }
;

setsymbol (RegexAstPart) ::=
	  char										{ $$ = new RegexAstChar($char, source, ${left().offset}, ${left().endoffset}); }
	| escaped									{ $$ = new RegexAstChar($escaped, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexAstCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder, reporter, ${left().offset}, ${left().endoffset}), source, ${left().offset}, ${left().endoffset}); }
;

%right char escaped;

charset (java.util.@List<RegexAstPart>) ::=
	  sym='-'									{ $$ = new java.util.@ArrayList<RegexAstPart>(); $charset.add(new RegexAstChar('-', source, ${sym.offset}, ${sym.endoffset})); }
	| setsymbol									{ $$ = new java.util.@ArrayList<RegexAstPart>(); RegexUtil.addSetSymbol($charset, $setsymbol, reporter); }
	| charset setsymbol							{ RegexUtil.addSetSymbol($charset#1, $setsymbol, reporter); }
	| charset sym='-'							{ $charset#1.add(new RegexAstChar('-', source, ${sym.offset}, ${sym.endoffset})); }
			%prio char
	| charset '-' char							{ RegexUtil.applyRange($charset#1, new RegexAstChar($char, source, ${char.offset}, ${char.endoffset}), reporter); }
	| charset '-' escaped						{ RegexUtil.applyRange($charset#1, new RegexAstChar($escaped, source, ${escaped.offset}, ${escaped.endoffset}), reporter); }
;

parts (RegexAstPart) ::=
	  part
	| list=parts part							{ $$ = RegexUtil.createSequence($list, $part); }
;


%%

${template java.classcode}
${call base-}
org.textmapper.lapg.regex.RegexDefTree.@TextSource source;
org.textmapper.lapg.common.@CharacterSetImpl.Builder setbuilder = new org.textmapper.lapg.common.@CharacterSetImpl.Builder();
${end}

${template java_lexer.lexercode}
private void quantifierReady() {
	if (chr == 0) {
		if (state == 1) state = 0;
		return;
	}
	if (state == 0) state = 1;
}
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}
