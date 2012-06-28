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
package org.textway.lapg.idea.lang.templates;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.templates.lexer.LtplLexerAdapter;
import org.textway.lapg.idea.lang.templates.lexer.LtplTokenTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Gryaznov Evgeny, 3/2/12
 */
public class LtplSyntaxHighlighter extends SyntaxHighlighterBase implements LtplTokenTypes {

	private static final Map<IElementType, TextAttributesKey> attributes;

	static {
		attributes = new HashMap<IElementType, TextAttributesKey>();

		fillMap(attributes, SyntaxHighlighterColors.STRING, CCON);
		fillMap(attributes, SyntaxHighlighterColors.NUMBER, ICON);
		fillMap(attributes, HighlighterColors.TEXT, IDENTIFIER);
		fillMap(attributes, CodeInsightColors.INSTANCE_FIELD_ATTRIBUTES, ESCID, ESCINT);
		fillMap(attributes, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT, ANY);

		// [] ()
		fillMap(attributes, SyntaxHighlighterColors.BRACKETS, OP_LSQUARE, OP_RSQUARE);
		fillMap(attributes, SyntaxHighlighterColors.PARENTHS, OP_LPAREN, OP_RPAREN);
		fillMap(attributes, SyntaxHighlighterColors.BRACES, OP_LCURLY, OP_RCURLY);

		// punctuation
		fillMap(attributes, SyntaxHighlighterColors.DOT, OP_DOT);
		fillMap(attributes, SyntaxHighlighterColors.COMMA, OP_COMMA);

		// operators/keywords
		fillMap(attributes, operators, SyntaxHighlighterColors.OPERATION_SIGN);
		fillMap(attributes, keywords, SyntaxHighlighterColors.KEYWORD);
	}

	@NotNull
	public Lexer getHighlightingLexer() {
		return new LtplLexerAdapter();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		return pack(attributes.get(tokenType));
	}
}
