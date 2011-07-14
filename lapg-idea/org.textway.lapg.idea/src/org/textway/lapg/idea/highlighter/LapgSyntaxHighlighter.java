/**
 * Copyright (c) 2010-2011 Evgeny Gryaznov
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
package org.textway.lapg.idea.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.textway.lapg.idea.lexer.LapgLexerAdapter;
import org.textway.lapg.idea.lexer.LapgTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LapgSyntaxHighlighter extends SyntaxHighlighterBase implements LapgTokenTypes {

	private static final Map<IElementType, TextAttributesKey> attributes;

	static {
		attributes = new HashMap<IElementType, TextAttributesKey>();

		fillMap(attributes, SyntaxHighlighterColors.LINE_COMMENT, COMMENT);
		fillMap(attributes, SyntaxHighlighterColors.STRING, STRING);
		fillMap(attributes, SyntaxHighlighterColors.NUMBER, ICON);
		fillMap(attributes, HighlighterColors.TEXT, IDENTIFIER);  // TODO fix
		fillMap(attributes, SyntaxHighlighterColors.VALID_STRING_ESCAPE, REGEXP); // TODO fix
		fillMap(attributes, SyntaxHighlighterColors.DOC_COMMENT, TEMPLATES, TOKEN_ACTION); // TODO fix

		// [] ()
		fillMap(attributes, SyntaxHighlighterColors.BRACKETS, OP_LBRACKET, OP_RBRACKET);
		fillMap(attributes, SyntaxHighlighterColors.PARENTHS, OP_LPAREN, OP_RPAREN);

		// punctuation
		fillMap(attributes, SyntaxHighlighterColors.DOT, OP_DOT);
		fillMap(attributes, SyntaxHighlighterColors.COMMA, OP_COMMA);
		fillMap(attributes, SyntaxHighlighterColors.JAVA_SEMICOLON, OP_SEMICOLON);

		// operators/keywords
		fillMap(attributes, operators, SyntaxHighlighterColors.OPERATION_SIGN);
		fillMap(attributes, keywords, SyntaxHighlighterColors.KEYWORD);
	}

	@NotNull
	public Lexer getHighlightingLexer() {
		return new LapgLexerAdapter();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		return pack(attributes.get(tokenType));
	}
}
