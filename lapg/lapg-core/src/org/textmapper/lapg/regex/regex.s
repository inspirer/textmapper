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

[0 1 2]

char(Character): /[^()\[\]\.|\\\/*?+-]/			{ $lexem = current().charAt(0); quantifierReady(); break; }
escaped(Character): /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/
												{ $lexem = current().charAt(1); quantifierReady(); break; }
escaped(Character): /\\a/						{ $lexem = (char) 7; quantifierReady(); break; }
escaped(Character): /\\b/						{ $lexem = '\b'; quantifierReady(); break; }
escaped(Character): /\\f/						{ $lexem = '\f'; quantifierReady(); break; }
escaped(Character): /\\n/						{ $lexem = '\n'; quantifierReady(); break; }
escaped(Character): /\\r/						{ $lexem = '\r'; quantifierReady(); break; }
escaped(Character): /\\t/						{ $lexem = '\t'; quantifierReady(); break; }
escaped(Character): /\\v/						{ $lexem = (char) 0xb; quantifierReady(); break; }
escaped(Character): /\\[0-7][0-7][0-7]/			{ $lexem = RegexUtil.unescapeOct(current().substring(1)); quantifierReady(); break; }
hx = /[0-9A-Fa-f]/
escaped(Character): /\\[xX]{hx}{hx}/			{ $lexem = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); break; }
escaped(Character): /\\[uU]{hx}{hx}{hx}{hx}/	{ $lexem = RegexUtil.unescapeHex(current().substring(2)); quantifierReady(); break; }
charclass(String): /\\[wWsSdD]/					{ $lexem = current().substring(1); quantifierReady(); break; }
charclass(String): /\\p\{\w+\}/					{ $lexem = current().substring(3, current().length() - 1); quantifierReady(); break; }

'.':  /\./										{ quantifierReady(); break; }

[1]

'*':  /*/										{ group = 0; break; }
'+':  /+/                                       { group = 0; break; }
'?':  /?/										{ group = 0; break; }
quantifier:  /\{[0-9]+(,[0-9]*)?\}/				{ group = 0; break; }

op_minus:		/\{-\}/							{ group = 0; break; }
op_union:		/\{+\}/							{ group = 0; break; }
op_intersect:	/\{&&\}/						{ group = 0; break; }

[0 2]

char(Character): /[*+?]/						{ $lexem = current().charAt(0); quantifierReady(); break; }

[0 1]

'(':  /\(/										{ group = 0; break; }
'|':  /\|/										{ group = 0; break; }
')':  /\)/										{ quantifierReady(); break; }

'(?':	/\(\?[is-]+:/							{ group = 0; break; }

'[':	/\[/									{ group = 2; break; }
'[^':	/\[^/									{ group = 2; break; }
char(Character):  /-/							{ $lexem = current().charAt(0); quantifierReady(); break; }

identifier = /[a-zA-Z_][a-zA-Z_\-0-9]*/

expand:			/\{{identifier}\}/	(class)		{ quantifierReady(); break; }
kw_eoi:			/\{eoi\}/						{ group = 0; break; }

[2]

']':  /\]/										{ group = 0; quantifierReady(); break; }
'-':  /-/
char(Character):  /[\(\|\)]/					{ $lexem = current().charAt(0); break; }

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
	| charclass									{ $$ = new RegexAstCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder), source, ${left().offset}, ${left().endoffset}); }
	| '.'										{ $$ = new RegexAstAny(source, ${left().offset}, ${left().endoffset}); }
	| '(' pattern ')'							{ $$ = RegexUtil.wrap($pattern); }
	| '[' charset ']'							{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, false); }
	| '[^' charset ']'							{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, true); }
	| expand									{ $$ = new RegexAstExpand(source, ${left().offset}, ${left().endoffset}); RegexUtil.checkExpand((RegexAstExpand) $$, reporter); }
;

setsymbol (RegexAstPart) ::=
	  char										{ $$ = new RegexAstChar($char, source, ${left().offset}, ${left().endoffset}); }
	| escaped									{ $$ = new RegexAstChar($escaped, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexAstCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder), source, ${left().offset}, ${left().endoffset}); }
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
		if (group == 1) group = 0;
		return;
	}
	if (group == 0) group = 1;
}
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}
