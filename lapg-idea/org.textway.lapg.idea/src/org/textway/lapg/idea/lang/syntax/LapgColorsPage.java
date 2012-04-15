/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
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
package org.textway.lapg.idea.lang.syntax;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.LapgIcons;

import javax.swing.*;
import java.util.Map;

/**
 * Gryaznov Evgeny, 4/8/12
 */
public class LapgColorsPage implements ColorSettingsPage {

	private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
			new AttributesDescriptor("Keyword", LapgSyntaxHighlighter.KEYWORD),
			new AttributesDescriptor("String", LapgSyntaxHighlighter.STRING),
			new AttributesDescriptor("Number", LapgSyntaxHighlighter.NUMBER),
			new AttributesDescriptor("Identifier", LapgSyntaxHighlighter.IDENTIFIER),
			new AttributesDescriptor("Operator sign", LapgSyntaxHighlighter.OPERATOR),
			new AttributesDescriptor("Brackets", LapgSyntaxHighlighter.BRACKETS),
			new AttributesDescriptor("Parenthesis", LapgSyntaxHighlighter.PARENTHS),
			new AttributesDescriptor("Lexem reference", LapgSyntaxHighlighter.LEXEM_REFERENCE),
			new AttributesDescriptor("Line comment", LapgSyntaxHighlighter.LINE_COMMENT),

			new AttributesDescriptor("RegExp Delimiters", LapgSyntaxHighlighter.RE_DELIMITERS),
			new AttributesDescriptor("RegExp Text", LapgSyntaxHighlighter.RE_TEXT),
			new AttributesDescriptor("RegExp Escaped character", LapgSyntaxHighlighter.RE_ESCAPED),
			new AttributesDescriptor("RegExp Character class", LapgSyntaxHighlighter.RE_CHAR_CLASS),
			new AttributesDescriptor("RegExp Dot (any character)", LapgSyntaxHighlighter.RE_DOT),
			new AttributesDescriptor("RegExp Quantifier", LapgSyntaxHighlighter.RE_QUANTIFIER),
//			new AttributesDescriptor("RegExp Invalid escape sequence", LapgSyntaxHighlighter.RE_INVALID),
//			new AttributesDescriptor("RegExp Redundant escape sequence", LapgSyntaxHighlighter.RE_REDUNDANT),
			new AttributesDescriptor("RegExp Brackets", LapgSyntaxHighlighter.RE_BRACKETS),
			new AttributesDescriptor("RegExp Parentheses", LapgSyntaxHighlighter.RE_PARENTHS),
			new AttributesDescriptor("RegExp Expand", LapgSyntaxHighlighter.RE_EXPAND),
//			new AttributesDescriptor("RegExp Comma", LapgSyntaxHighlighter.RE_COMMA),
			new AttributesDescriptor("RegExp Bad character", LapgSyntaxHighlighter.RE_BAD_CHAR),
//			new AttributesDescriptor("RegExp Quote character", LapgSyntaxHighlighter.QUOTE_CHARACTER),
	};

	@NonNls
	private static final Map<String, TextAttributesKey> ourTagToDescriptorMap = new HashMap<String, TextAttributesKey>();

	static {
		ourTagToDescriptorMap.put("lexemRef", LapgSyntaxHighlighter.LEXEM_REFERENCE);
	}

	@NotNull
	public String getDisplayName() {
		return "Lapg";
	}

	public Icon getIcon() {
		return LapgIcons.LAPG_ICON;
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
		final SyntaxHighlighter highlighter = SyntaxHighlighter.PROVIDER.create(LapgFileType.LAPG_FILE_TYPE, null, null);
		assert highlighter != null;
		return highlighter;
	}

	@NotNull
	public String getDemoText() {
		return "lang = \"java\"\n" +
				"prefix = \"Test\"\n" +
				"gentree = true\n" +
				"maxtoken = 2048\n" +
				"\n" +
				"[0]\n" +
				"idStart = /[a-zA-Z_]/\n" +
				"identifier(String): /{idStart}([A-Za-z_\\d])*/  (class)\n" +
				"                 { $lexem = current(); break; }\n" +
				"kw_eval:  /eval/ (soft)\n" +
				"'*': /*/\n" +
				"'+': /+/\n" +
				"complex: /\\p{Lu}-a{1,8}-[^a-z] \\y . forwardSlash:\\/ /\n" +
				"skip: /[\\t\\r\\n ]+/ (space)\n" +
				"\n" +
				"# Grammar\n" +
				"\n" +
				"%input root;\n" +
				"%left '+';\n" +
				"%left '*';\n" +
				"\n" +
				"root (ParsedRoot) ::=\n" +
				"      <lexemRef>kw_eval</lexemRef> expr  {  $$ = new ParsedRoot($expr, ${root.offset}, ${root.endoffset}); }\n" +
				";\n" +
				"\n" +
				"expr ::=\n" +
				"      identifier\n" +
				"    | expr '+' expr\n" +
				"    | expr '*' expr\n" +
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
