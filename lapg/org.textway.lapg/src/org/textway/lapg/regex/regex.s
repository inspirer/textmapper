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
package = "org.textway.lapg.regex"
breaks = true
gentree = true
positions = "offset"
endpositions = "offset"
genCopyright = true

char(Character): /[^(){}\[\]\.\|\\\/*?+^-]/      							{ $lexem = current().charAt(0); break; }
char(Character): /\\[^\r\n\t0-9uUxXwWsSdDpPabfnrtv]/						{ $lexem = current().charAt(1); break; }
char(Character): /\\a/														{ $lexem = (char) 7; break; }
char(Character): /\\b/														{ $lexem = '\b'; break; }
char(Character): /\\f/														{ $lexem = '\f'; break; }
char(Character): /\\n/														{ $lexem = '\n'; break; }
char(Character): /\\r/														{ $lexem = '\r'; break; }
char(Character): /\\t/														{ $lexem = '\t'; break; }
char(Character): /\\v/														{ $lexem = (char) 0xb; break; }
char(Character): /\\[0-7][0-7][0-7]/										{ $lexem = RegexUtil.unescapeOct(current().substring(1)); break; }
char(Character): /\\[xX][0-9A-Fa-f][0-9A-Fa-f]/								{ $lexem = RegexUtil.unescapeHex(current().substring(2)); break; }
char(Character): /\\[uU][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]/		{ $lexem = RegexUtil.unescapeHex(current().substring(2)); break; }
charclass(String): /\\[wWsSdD]/												{ $lexem = current().substring(1); break; }
charclass(String): /\\p\{\w+\}/												{ $lexem = current().substring(3, current().length() - 1); break; }

'.':  /\./
'-':  /-/
'^':  /^/
'(':  /\(/
'|':  /\|/
')':  /\)/
'{':  /\{/
'{digit'(Character):  /\{[0-9]/                                             { $lexem = current().charAt(1); break; }
'{letter'(Character): /\{[a-zA-Z_]/                                         { $lexem = current().charAt(1); break; }

'}':  /\}/
'[':  /\[/
']':  /\]/
'*':  /*/
'+':  /+/
'?':  /?/


# grammar

%input pattern;

pattern (RegexPart) ::=
	  partsopt									{ $$ = RegexUtil.emptyIfNull($partsopt, source, ${partsopt.offset}); }
	| left=pattern '|' partsopt					{ $$ = RegexUtil.createOr($left, $partsopt, source, ${partsopt.offset}); }
;

%right '*' '+' '?';

part (RegexPart) ::=
	  primitive_part 		%prio '*'
	| primitive_part '*'						{ $$ = new RegexQuantifier($primitive_part, 0, -1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part '+'						{ $$ = new RegexQuantifier($primitive_part, 1, -1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part '?'						{ $$ = new RegexQuantifier($primitive_part, 0, 1, source, ${left().offset}, ${left().endoffset}); }
	| primitive_part op='{digit' sconopt '}'	{ $$ = RegexUtil.createQuantifier($primitive_part, source, ${op.offset}, ${left().endoffset}, reporter); }
;

primitive_part (RegexPart) ::=
	  char										{ $$ = new RegexChar($char, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder), source, ${left().offset}, ${left().endoffset}); }
	| '.'										{ $$ = new RegexAny(source, ${left().offset}, ${left().endoffset}); }
	| '-'                                       { $$ = new RegexChar('-', source, ${left().offset}, ${left().endoffset}); }
	| '^'                                       { $$ = new RegexChar('^', source, ${left().offset}, ${left().endoffset}); }
	| '*'                                       { $$ = new RegexChar('*', source, ${left().offset}, ${left().endoffset}); }
	| '+'                                       { $$ = new RegexChar('+', source, ${left().offset}, ${left().endoffset}); }
	| '?'                                       { $$ = new RegexChar('?', source, ${left().offset}, ${left().endoffset}); }
	| '(' pattern ')'							{ $$ = RegexUtil.wrap($pattern); }
	| '[' charset ']'							{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, false); }
	| '[' '^' charset ']'						{ $$ = RegexUtil.toSet($charset, reporter, setbuilder, true); }
	| '{letter' sconopt '}'						{ $$ = new RegexExpand(source, ${left().offset}, ${left().endoffset}); RegexUtil.checkExpand((RegexExpand) $$, reporter); }
;

setsymbol (RegexPart) ::=
	  char										{ $$ = new RegexChar($char, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder), source, ${left().offset}, ${left().endoffset}); }
	| '('										{ $$ = new RegexChar('(', source, ${left().offset}, ${left().endoffset}); }
	| '|'                                       { $$ = new RegexChar('|', source, ${left().offset}, ${left().endoffset}); }
	| ')'                                       { $$ = new RegexChar(')', source, ${left().offset}, ${left().endoffset}); }
	| '{'                                       { $$ = new RegexChar('{', source, ${left().offset}, ${left().endoffset}); }
	| '{digit'                                  { $$ = RegexUtil.createOr(new RegexChar('{', source, ${left().offset}, ${left().offset}+1),
																		  new RegexChar($0, source, ${left().offset}+1, ${left().endoffset}), null, 0); }
	| '{letter'                                 { $$ = RegexUtil.createOr(new RegexChar('{', source, ${left().offset}, ${left().offset}+1),
																		  new RegexChar($0, source, ${left().offset}+1, ${left().endoffset}), null, 0); }
	| '}'                                       { $$ = new RegexChar('}', source, ${left().offset}, ${left().endoffset}); }
	| '*'                                       { $$ = new RegexChar('*', source, ${left().offset}, ${left().endoffset}); }
	| '+'                                       { $$ = new RegexChar('+', source, ${left().offset}, ${left().endoffset}); }
	| '?'                                       { $$ = new RegexChar('?', source, ${left().offset}, ${left().endoffset}); }
;

%right char;

charset (java.util.@List<RegexPart>) ::=
	  sym='-'									{ $$ = new java.util.@ArrayList<RegexPart>(); $charset.add(new RegexChar('-', source, ${sym.offset}, ${sym.endoffset})); }
	| setsymbol									{ $$ = new java.util.@ArrayList<RegexPart>(); RegexUtil.addSetSymbol($charset, $setsymbol, reporter); }
	| charset setsymbol							{ RegexUtil.addSetSymbol($charset#1, $setsymbol, reporter); }
	| charset sym='^'							{ $charset#1.add(new RegexChar('^', source, ${sym.offset}, ${sym.endoffset})); }
	| charset sym='-'							{ $charset#1.add(new RegexChar('-', source, ${sym.offset}, ${sym.endoffset})); }
			%prio char
	| charset '-' char							{ RegexUtil.applyRange($charset#1, new RegexChar($char, source, ${char.offset}, ${char.endoffset}), reporter); }
;

parts (RegexPart) ::=
	  part
	| list=parts part							{ $$ = RegexUtil.createSequence($list, $part); }
;

scon ::=
	  char
	| '-'
	| scon char
	| scon '-'
;


%%

${template java.classcode}
${call base-}
org.textway.lapg.regex.RegexDefTree.@TextSource source;
org.textway.lapg.api.regex.@CharacterSet.Builder setbuilder = new org.textway.lapg.api.regex.@CharacterSet.Builder();
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}
