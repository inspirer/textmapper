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
package org.textmapper.idea.lang.syntax;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.regex.lexer.RegexTokenTypes;
import org.textmapper.idea.lang.syntax.lexer.TMHighlightingLexer;
import org.textmapper.idea.lang.syntax.lexer.TMTokenTypes;
import org.textmapper.idea.lang.templates.LtplFileType;
import org.textmapper.idea.lang.templates.LtplSyntaxHighlighter;

import java.util.HashMap;
import java.util.Map;

public class TMSyntaxHighlighter extends SyntaxHighlighterBase {

	private LtplSyntaxHighlighter fTemplatesHighlighter = new LtplSyntaxHighlighter();

	static final TextAttributesKey KEYWORD =
			TextAttributesKey.createTextAttributesKey("TM.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

	static final TextAttributesKey STRING =
			TextAttributesKey.createTextAttributesKey("TM.STRING", DefaultLanguageHighlighterColors.STRING);

	static final TextAttributesKey NUMBER =
			TextAttributesKey.createTextAttributesKey("TM.NUMBER", DefaultLanguageHighlighterColors.NUMBER);

	static final TextAttributesKey IDENTIFIER =
			TextAttributesKey.createTextAttributesKey("TM.IDENTIFIER", HighlighterColors.TEXT);

	static final TextAttributesKey OPERATOR =
			TextAttributesKey.createTextAttributesKey("TM.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

	static final TextAttributesKey BRACKETS =
			TextAttributesKey.createTextAttributesKey("TM.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

	static final TextAttributesKey PARENTHS =
			TextAttributesKey.createTextAttributesKey("TM.PARENTHS", DefaultLanguageHighlighterColors.PARENTHESES);

	static final TextAttributesKey BRACES =
			TextAttributesKey.createTextAttributesKey("TM.BRACES", DefaultLanguageHighlighterColors.BRACES);

	static final TextAttributesKey QUANTIFIER =
			TextAttributesKey.createTextAttributesKey("TM.QUANTIFIER", DefaultLanguageHighlighterColors.KEYWORD);

	static final TextAttributesKey LEXEM_REFERENCE =
			TextAttributesKey.createTextAttributesKey("TM.LEXEM_REFERENCE", CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES);

	static final TextAttributesKey LINE_COMMENT =
			TextAttributesKey.createTextAttributesKey("TM.LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

	static final TextAttributesKey ANNOTATION =
			TextAttributesKey.createTextAttributesKey("TM.ANNOTATION", DefaultLanguageHighlighterColors.METADATA);

	static final TextAttributesKey SECTION =
			TextAttributesKey.createTextAttributesKey("TM.SECTION", DefaultLanguageHighlighterColors.STRING);

	static final TextAttributesKey RULE_METADATA =
			TextAttributesKey.createTextAttributesKey("TM.RULE_METADATA", DefaultLanguageHighlighterColors.NUMBER);

	static final TextAttributesKey NONTERM_PARAMETER_NAME =
			TextAttributesKey.createTextAttributesKey("TM.NONTERM_PARAMETER", CodeInsightColors.TYPE_PARAMETER_NAME_ATTRIBUTES);

	// Regexp

	static final TextAttributesKey RE_DELIMITERS =
			TextAttributesKey.createTextAttributesKey("TM.RE_DELIMITERS", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);

	static final TextAttributesKey RE_TEXT =
			TextAttributesKey.createTextAttributesKey("TM.RE_TEXT", HighlighterColors.TEXT);

	static final TextAttributesKey RE_ESCAPED =
			TextAttributesKey.createTextAttributesKey("TM.RE_ESCAPED", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);

	static final TextAttributesKey RE_CHAR_CLASS =
			TextAttributesKey.createTextAttributesKey("TM.RE_CHAR_CLASS", CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES);

	static final TextAttributesKey RE_DOT =
			TextAttributesKey.createTextAttributesKey("TM.RE_DOT", DefaultLanguageHighlighterColors.KEYWORD);

	static final TextAttributesKey RE_QUANTIFIER =
			TextAttributesKey.createTextAttributesKey("TM.RE_QUANTIFIER", DefaultLanguageHighlighterColors.KEYWORD);

	static final TextAttributesKey RE_BRACKETS =
			TextAttributesKey.createTextAttributesKey("TM.RE_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

	static final TextAttributesKey RE_PARENTHS =
			TextAttributesKey.createTextAttributesKey("TM.RE_PARENTHS", DefaultLanguageHighlighterColors.PARENTHESES);

	static final TextAttributesKey RE_EXPAND =
			TextAttributesKey.createTextAttributesKey("TM.RE_EXPAND", CodeInsightColors.STATIC_FIELD_ATTRIBUTES);

	static final TextAttributesKey RE_BAD_CHAR =
			TextAttributesKey.createTextAttributesKey("TM.RE_BAD_CHAR", HighlighterColors.BAD_CHARACTER);

	private static final Map<IElementType, TextAttributesKey> attributes;

	static {
		attributes = new HashMap<IElementType, TextAttributesKey>();

		fillMap(attributes, TMTokenTypes.keywords, KEYWORD);
		fillMap(attributes, TMTokenTypes.softKeywords, IDENTIFIER);
		fillMap(attributes, STRING, TMTokenTypes.STRING);
		fillMap(attributes, NUMBER, TMTokenTypes.ICON);
		fillMap(attributes, IDENTIFIER, TMTokenTypes.ID);
		fillMap(attributes, LINE_COMMENT, TMTokenTypes.COMMENT);

		// [] () and operators
		fillMap(attributes, TMTokenTypes.operators, OPERATOR);
		fillMap(attributes, TMTokenTypes.quantifiers, QUANTIFIER);
		fillMap(attributes, BRACKETS, TMTokenTypes.OP_LBRACKET, TMTokenTypes.OP_RBRACKET);
		fillMap(attributes, PARENTHS, TMTokenTypes.OP_LPAREN, TMTokenTypes.OP_RPAREN);
		fillMap(attributes, BRACES, TMTokenTypes.OP_LCURLYTILDE, TMTokenTypes.OP_LCURLY, TMTokenTypes.OP_RCURLY);

		// punctuation
		fillMap(attributes, DefaultLanguageHighlighterColors.DOT, TMTokenTypes.OP_DOT);
		fillMap(attributes, DefaultLanguageHighlighterColors.COMMA, TMTokenTypes.OP_COMMA);
		fillMap(attributes, DefaultLanguageHighlighterColors.SEMICOLON, TMTokenTypes.OP_SEMICOLON);
		fillMap(attributes, SECTION, TMTokenTypes.OP_COLONCOLON, TMTokenTypes.KW_LEXER_ACC, TMTokenTypes.KW_PARSER_ACC);

		// regexp
		fillMap(attributes, RE_DELIMITERS, RegexTokenTypes.RE_DELIMITERS);
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
		fillMap(attributes, RE_BAD_CHAR, RegexTokenTypes.RE_BAD);
	}

	@NotNull
	public Lexer getHighlightingLexer() {
		return new TMHighlightingLexer();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		if (tokenType.getLanguage() == LtplFileType.LTPL_LANGUAGE) {
			return fTemplatesHighlighter.getTokenHighlights(tokenType);
		}
		return pack(attributes.get(tokenType));
	}
}
