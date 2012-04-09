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

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.regex.lexer.RegexTokenTypes;
import org.textway.lapg.idea.lang.syntax.lexer.LapgHighlightingLexer;
import org.textway.lapg.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textway.lapg.idea.lang.templates.LtplFileType;
import org.textway.lapg.idea.lang.templates.LtplSyntaxHighlighter;

import java.util.HashMap;
import java.util.Map;

public class LapgSyntaxHighlighter extends SyntaxHighlighterBase implements LapgTokenTypes, RegexTokenTypes {

	private LtplSyntaxHighlighter fTemplatesHighlighter = new LtplSyntaxHighlighter();

	static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(
			"LAPG.KEYWORD",
			SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
	);

	static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey(
			"LAPG.STRING",
			SyntaxHighlighterColors.STRING.getDefaultAttributes()
	);

	static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey(
			"LAPG.NUMBER",
			SyntaxHighlighterColors.NUMBER.getDefaultAttributes()
	);

	static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey(
			"LAPG.IDENTIFIER",
			HighlighterColors.TEXT.getDefaultAttributes()
	);

	static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey(
			"LAPG.OPERATOR",
			SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes()
	);

	static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey(
			"LAPG.BRACKETS",
			SyntaxHighlighterColors.BRACKETS.getDefaultAttributes()
	);

	static final TextAttributesKey PARENTHS = TextAttributesKey.createTextAttributesKey(
			"LAPG.PARENTHS",
			SyntaxHighlighterColors.PARENTHS.getDefaultAttributes()
	);

	static final TextAttributesKey LEXEM_REFERENCE = TextAttributesKey.createTextAttributesKey(
			"LAPG.LEXEM_REFERENCE",
			CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES.getDefaultAttributes()
	);

	static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey(
			"LAPG.LINE_COMMENT",
			SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes()
	);

	// Regexp

	static final TextAttributesKey RE_TEXT = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_TEXT",
			SyntaxHighlighterColors.VALID_STRING_ESCAPE.getDefaultAttributes()
	);

	static final TextAttributesKey RE_ESCAPED = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_ESCAPED",
			SyntaxHighlighterColors.VALID_STRING_ESCAPE.getDefaultAttributes()
	);

	static final TextAttributesKey RE_CHAR_CLASS = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_CHAR_CLASS",
			SyntaxHighlighterColors.VALID_STRING_ESCAPE.getDefaultAttributes()
	);

	static final TextAttributesKey RE_DOT = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_DOT",
			SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
	);

	static final TextAttributesKey RE_QUANTIFIER = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_QUANTIFIER",
			SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
	);

	static final TextAttributesKey RE_BRACKETS = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_BRACKETS",
			SyntaxHighlighterColors.BRACKETS.getDefaultAttributes()
	);

	static final TextAttributesKey RE_PARENTHS = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_PARENTHS",
			SyntaxHighlighterColors.PARENTHS.getDefaultAttributes()
	);

	static final TextAttributesKey RE_EXPAND = TextAttributesKey.createTextAttributesKey(
			"LAPG.RE_EXPAND",
			CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES.getDefaultAttributes()
	);

	private static final Map<IElementType, TextAttributesKey> attributes;

	static {
		attributes = new HashMap<IElementType, TextAttributesKey>();

		fillMap(attributes, LapgTokenTypes.keywords, KEYWORD);
		fillMap(attributes, STRING, LapgTokenTypes.STRING);
		fillMap(attributes, NUMBER, LapgTokenTypes.ICON);
		fillMap(attributes, IDENTIFIER, LapgTokenTypes.IDENTIFIER);
		fillMap(attributes, LINE_COMMENT, LapgTokenTypes.COMMENT);

		// [] () and operators
		fillMap(attributes, LapgTokenTypes.operators, OPERATOR);
		fillMap(attributes, BRACKETS, LapgTokenTypes.OP_LBRACKET, LapgTokenTypes.OP_RBRACKET);
		fillMap(attributes, PARENTHS, LapgTokenTypes.OP_LPAREN, LapgTokenTypes.OP_RPAREN);

		// punctuation
		fillMap(attributes, SyntaxHighlighterColors.DOT, LapgTokenTypes.OP_DOT);
		fillMap(attributes, SyntaxHighlighterColors.COMMA, LapgTokenTypes.OP_COMMA);
		fillMap(attributes, SyntaxHighlighterColors.JAVA_SEMICOLON, LapgTokenTypes.OP_SEMICOLON);

		// regexp
		fillMap(attributes, RE_TEXT, RegexTokenTypes.RE_CHAR);
		fillMap(attributes, RE_ESCAPED, RegexTokenTypes.RE_ESCAPED);
		fillMap(attributes, RE_CHAR_CLASS, RegexTokenTypes.RE_CHARCLASS);
		fillMap(attributes, RE_DOT, RegexTokenTypes.RE_DOT);
		fillMap(attributes, RE_QUANTIFIER,
				RegexTokenTypes.RE_MULT, RegexTokenTypes.RE_PLUS, RegexTokenTypes.RE_QUESTIONMARK,
				RegexTokenTypes.RE_QUANTFIER);
		fillMap(attributes, RE_BRACKETS,
				RegexTokenTypes.RE_LSQUARE, RegexTokenTypes.RE_LSQUAREXOR, RegexTokenTypes.RE_RSQUARE);
		fillMap(attributes, RE_PARENTHS, RegexTokenTypes.RE_LPAREN, RegexTokenTypes.RE_RPAREN);
		fillMap(attributes, RE_EXPAND, RegexTokenTypes.RE_EXPAND);
	}

	@NotNull
	public Lexer getHighlightingLexer() {
		return new LapgHighlightingLexer();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		if (tokenType.getLanguage() == LtplFileType.LTPL_LANGUAGE) {
			return fTemplatesHighlighter.getTokenHighlights(tokenType);
		}
		return pack(attributes.get(tokenType));
	}
}
