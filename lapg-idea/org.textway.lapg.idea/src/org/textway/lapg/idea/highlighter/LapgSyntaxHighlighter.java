/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
