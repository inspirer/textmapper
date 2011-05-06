# lapg syntax file

lang = "java"
prefix = "RegexDef"
package = "org.textway.lapg.regex"
breaks = true
gentree = true
positions = "offset"
endpositions = "offset"

char(Character): /[^(){}\[\]\.\|\\\/*?+^-]/      							{ $lexem = current().charAt(0); break; }
char(Character): /\\[^\r\n\t0-9uUxXwWsSdD]/									{ $lexem = RegexUtil.unescape(current().charAt(1)); break; }
char(Character): /\\[0-7][0-7][0-7]/										{ $lexem = RegexUtil.unescapeOct(current().substring(1)); break; }
char(Character): /\\[xX][0-9A-Fa-f][0-9A-Fa-f]/								{ $lexem = RegexUtil.unescapeHex(current().substring(2)); break; }
char(Character): /\\[uU][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]/		{ $lexem = RegexUtil.unescapeHex(current().substring(2)); break; }
charclass(Character): /\\[wWsSdD]/											{ $lexem = current().charAt(1); break; }

'.':  /\./
'-':  /-/
'^':  /^/
'(':  /\(/
'|':  /\|/
')':  /\)/
'{':  /\{/
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
	| primitive_part opening='{' scon '}'		{ $$ = RegexUtil.createQuantifierOrSequence($primitive_part, new RegexExpand(source, ${opening.offset}, ${left().endoffset}), reporter); }
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
;

setsymbol (RegexPart) ::=
	  char										{ $$ = new RegexChar($char, source, ${left().offset}, ${left().endoffset}); }
	| charclass									{ $$ = new RegexCharClass($charclass, RegexUtil.getClassSet($charclass, setbuilder), source, ${left().offset}, ${left().endoffset}); }
	| '('										{ $$ = new RegexChar('(', source, ${left().offset}, ${left().endoffset}); }
	| '|'                                       { $$ = new RegexChar('|', source, ${left().offset}, ${left().endoffset}); }
	| ')'                                       { $$ = new RegexChar(')', source, ${left().offset}, ${left().endoffset}); }
	| '{'                                       { $$ = new RegexChar('{', source, ${left().offset}, ${left().endoffset}); }
	| '}'                                       { $$ = new RegexChar('}', source, ${left().offset}, ${left().endoffset}); }
	| '*'                                       { $$ = new RegexChar('*', source, ${left().offset}, ${left().endoffset}); }
	| '+'                                       { $$ = new RegexChar('+', source, ${left().offset}, ${left().endoffset}); }
	| '?'                                       { $$ = new RegexChar('?', source, ${left().offset}, ${left().endoffset}); }
;

%right char;

charset (java.util.@List<RegexPart>) ::=
	  sym='-'									{ $$ = new java.util.@ArrayList<RegexPart>(); $charset.add(new RegexChar('-', source, ${sym.offset}, ${sym.endoffset})); }
	| setsymbol									{ $$ = new java.util.@ArrayList<RegexPart>(); $charset.add($setsymbol); }
	| charset setsymbol							{ $charset#1.add($setsymbol); }
	| charset sym='^'							{ $charset#1.add(new RegexChar('^', source, ${sym.offset}, ${sym.endoffset})); }
	| charset sym='-'							{ $charset#1.add(new RegexChar('-', source, ${sym.offset}, ${sym.endoffset})); }
			%prio char
	| charset '-' char							{ RegexUtil.applyRange($charset#1, new RegexChar($char, source, ${char.offset}, ${char.endoffset}), reporter); }
;

parts (RegexPart) ::=
	  part
	| '{' scon '}'								{ $$ = new RegexExpand(source, ${left().offset}, ${left().endoffset}); RegexUtil.checkExpand((RegexExpand) $$, reporter, false); }
	| list=parts part							{ $$ = RegexUtil.createSequence($list, $part); }
;

scon ::=
	  char
	| scon char
	| scon '.'
;


%%

${template java.classcode}
${call base-}
org.textway.lapg.regex.RegexDefTree.@TextSource source;
org.textway.lapg.lex.@CharacterSet.Builder setbuilder = new org.textway.lapg.lex.@CharacterSet.Builder();
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}
