/**
 * Copyright 2010-2017 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.TMIcons;

import javax.swing.*;
import java.util.Map;

/**
 * Gryaznov Evgeny, 4/8/12
 */
public class TMColorsPage implements ColorSettingsPage {

	private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
			new AttributesDescriptor("Keyword", TMSyntaxHighlighter.KEYWORD),
			new AttributesDescriptor("String", TMSyntaxHighlighter.STRING),
			new AttributesDescriptor("Number", TMSyntaxHighlighter.NUMBER),
			new AttributesDescriptor("Identifier", TMSyntaxHighlighter.IDENTIFIER),
			new AttributesDescriptor("Operator sign", TMSyntaxHighlighter.OPERATOR),
			new AttributesDescriptor("Quantifier", TMSyntaxHighlighter.QUANTIFIER),
			new AttributesDescriptor("Brackets", TMSyntaxHighlighter.BRACKETS),
			new AttributesDescriptor("Parenthesis", TMSyntaxHighlighter.PARENTHS),
			new AttributesDescriptor("Lexem reference", TMSyntaxHighlighter.LEXEM_REFERENCE),
			new AttributesDescriptor("Line comment", TMSyntaxHighlighter.LINE_COMMENT),
			new AttributesDescriptor("Annotations", TMSyntaxHighlighter.ANNOTATION),
			new AttributesDescriptor("Lookahead", TMSyntaxHighlighter.LOOKAHEAD),
			new AttributesDescriptor("Sections", TMSyntaxHighlighter.SECTION),
			new AttributesDescriptor("Rule metadata", TMSyntaxHighlighter.RULE_METADATA),
			new AttributesDescriptor("State marker", TMSyntaxHighlighter.STATE_MARKER),
			new AttributesDescriptor("Parameter Name", TMSyntaxHighlighter.NONTERM_PARAMETER_NAME),
			new AttributesDescriptor("Start condition", TMSyntaxHighlighter.START_CONDITION),

			new AttributesDescriptor("RegExp Delimiters", TMSyntaxHighlighter.RE_DELIMITERS),
			new AttributesDescriptor("RegExp Text", TMSyntaxHighlighter.RE_TEXT),
			new AttributesDescriptor("RegExp Escaped character", TMSyntaxHighlighter.RE_ESCAPED),
			new AttributesDescriptor("RegExp Character class", TMSyntaxHighlighter.RE_CHAR_CLASS),
			new AttributesDescriptor("RegExp Dot (any character)", TMSyntaxHighlighter.RE_DOT),
			new AttributesDescriptor("RegExp Quantifier", TMSyntaxHighlighter.RE_QUANTIFIER),
//			new AttributesDescriptor("RegExp Invalid escape sequence", TMSyntaxHighlighter.RE_INVALID),
//			new AttributesDescriptor("RegExp Redundant escape sequence", TMSyntaxHighlighter.RE_REDUNDANT),
			new AttributesDescriptor("RegExp Brackets", TMSyntaxHighlighter.RE_BRACKETS),
			new AttributesDescriptor("RegExp Parentheses", TMSyntaxHighlighter.RE_PARENTHS),
			new AttributesDescriptor("RegExp Expand", TMSyntaxHighlighter.RE_EXPAND),
//			new AttributesDescriptor("RegExp Comma", TMSyntaxHighlighter.RE_COMMA),
			new AttributesDescriptor("RegExp Bad character", TMSyntaxHighlighter.RE_BAD_CHAR),
//			new AttributesDescriptor("RegExp Quote character", TMSyntaxHighlighter.QUOTE_CHARACTER),
	};

	@NonNls
	private static final Map<String, TextAttributesKey> ourTagToDescriptorMap = new HashMap<>();

	static {
		ourTagToDescriptorMap.put("lexemeRef", TMSyntaxHighlighter.LEXEM_REFERENCE);
		ourTagToDescriptorMap.put("annotation", TMSyntaxHighlighter.ANNOTATION);
		ourTagToDescriptorMap.put("ruleMeta", TMSyntaxHighlighter.RULE_METADATA);
		ourTagToDescriptorMap.put("sect", TMSyntaxHighlighter.SECTION);
		ourTagToDescriptorMap.put("kw", TMSyntaxHighlighter.KEYWORD);
		ourTagToDescriptorMap.put("param", TMSyntaxHighlighter.NONTERM_PARAMETER_NAME);
		ourTagToDescriptorMap.put("lookahead", TMSyntaxHighlighter.LOOKAHEAD);
		ourTagToDescriptorMap.put("stateMarker", TMSyntaxHighlighter.STATE_MARKER);
		ourTagToDescriptorMap.put("startCond", TMSyntaxHighlighter.START_CONDITION);
	}

	@NotNull
	public String getDisplayName() {
		return "Textmapper";
	}

	public Icon getIcon() {
		return TMIcons.TM_ICON;
	}

	@NotNull
	public AttributesDescriptor[] getAttributeDescriptors() {
		return ATTRS;
	}

	@NotNull
	public ColorDescriptor[] getColorDescriptors() {
		return ColorDescriptor.EMPTY_ARRAY;
	}

	@NotNull
	public SyntaxHighlighter getHighlighter() {
		final SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(TMFileType.INSTANCE, null, null);
		assert highlighter != null;
		return highlighter;
	}

	@NotNull
	public String getDemoText() {
		return "<kw>language</kw> demo(java);\n" +
				"\n" +
				"prefix = \"Test\"\n" +
				"gentree = <kw>true</kw>\n" +
				"maxtoken = 2048\n" +
				"\n" +
				"<sect>:: lexer</sect>\n" +
				"\n" +
				"<initial> { \n" +
				"  idStart = /[a-zA-Z_]/\n" +
				"}\n" +
				"<<startCond>initial</startCond>> identifier {String}: /{idStart}([A-Za-z_\\d])*/  (<kw>class</kw>)\n" +
				"                 { $lexem = current(); break; }\n" +
				"kw_eval:  /eval/ (<kw>soft</kw>)\n" +
				"'(': /\\(/\n" +
				"')': /\\)/\n" +
				"',': /,/\n" +
				"'*': /*/\n" +
				"'+': /+/\n" +
				"complex: /\\p{Lu}-a{1,8}-[^a-z] \\y . forwardSlash:\\/ /\n" +
				"skip: /[\\t\\r\\n ]+/ (<kw>space</kw>)\n" +
				"\n" +
				"<sect>:: parser</sect>\n" +
				"\n" +
				"%<kw>param</kw> X <kw>symbol</kw>;\n" +
				"\n" +
				"parenthesized<<param>X</param>> : <lexemeRef>'('</lexemeRef> <param>X</param> <lexemeRef>')'</lexemeRef>;\n" +
				"\n" +
				"%<kw>input</kw> root;\n" +
				"%<kw>left</kw> <lexemeRef>'+'</lexemeRef>;\n" +
				"%<kw>left</kw> <lexemeRef>'*'</lexemeRef>;\n" +
				"\n" +
				"root (ParsedRoot) :\n" +
				"      <lexemeRef>kw_eval</lexemeRef> .<stateMarker>state1</stateMarker> expr  {  $$ = new ParsedRoot($expr, ${root.offset}, ${root.endoffset}); }\n" +
				";\n" +
				"\n" +
				"# expression rule\n" +
				"\n" +
				"<annotation>@noast</annotation>{<kw>true</kw>}\n" +
				"expr :\n" +
				"      <lexemeRef>identifier</lexemeRef>\n" +
				"    | expr <lexemeRef>'+'</lexemeRef> expr\n" +
				"    | expr <lexemeRef>'*'</lexemeRef> expr  <ruleMeta>-> binary/multiply</ruleMeta>\n" +
				"    | <lookahead>(?= StartOfA & StartOfB)</lookahead> <lexemeRef>identifier</lexemeRef> <lexemeRef>'('</lexemeRef> (expr <kw>separator</kw> <lexemeRef>','</lexemeRef>)* <lexemeRef>')'</lexemeRef>\n" +
				";\n" +
				"%%\n" +
				"\n" +
				"${template java.imports-}\n" +
				"${call base-}\n" +
				"import java.util.List;\n" +
				"${end}\n";

	}

	public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return ourTagToDescriptorMap;
	}
}
