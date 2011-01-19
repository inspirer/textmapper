/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface LapgTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	IElementType COMMENT = new LapgElementType("comment");
	TokenSet comments = TokenSet.create(COMMENT);

	// tokens
	IElementType STRING = new LapgElementType("string");
	IElementType ICON = new LapgElementType("int");
	IElementType IDENTIFIER = new LapgElementType("identifier");
	IElementType REGEXP = new LapgElementType("regexp");

	TokenSet strings = TokenSet.create(STRING);

	// inner tokens
	IElementType ACTION = new LapgElementType("action");
	IElementType TEMPLATES = new LapgElementType("templates");

	// [] ()
	IElementType OP_LBRACKET = new LapgElementType("[");
	IElementType OP_RBRACKET = new LapgElementType("]");
	IElementType OP_LPAREN = new LapgElementType("(");
	IElementType OP_RPAREN = new LapgElementType(")");

	// punctuation
	IElementType OP_SEMICOLON = new LapgElementType(";");
	IElementType OP_DOT = new LapgElementType(".");
	IElementType OP_COMMA = new LapgElementType(",");

	// operators
	IElementType OP_PERCENT = new LapgElementType("%");
	IElementType OP_CCEQ = new LapgElementType("::=");
	IElementType OP_OR = new LapgElementType("|");
	IElementType OP_EQ = new LapgElementType("=");
	IElementType OP_EQGT = new LapgElementType("=>");
	IElementType OP_COLON = new LapgElementType(":");
	IElementType OP_LTLT = new LapgElementType("<<");
	IElementType OP_LT = new LapgElementType("<");
	IElementType OP_GT = new LapgElementType(">");
	IElementType OP_STAR = new LapgElementType("*");
	IElementType OP_PLUS = new LapgElementType("+");
	IElementType OP_QMARK = new LapgElementType("?");
	IElementType OP_AND = new LapgElementType("&");
	IElementType OP_AT = new LapgElementType("@");

	TokenSet operators = TokenSet.create(
			OP_PERCENT, OP_CCEQ, OP_OR, OP_EQ, OP_EQGT, OP_COLON,
			OP_LTLT, OP_LT, OP_GT, OP_STAR, OP_PLUS, OP_QMARK, OP_AND, OP_AT
	);

	// keywords
	IElementType KW_TRUE = new LapgElementType("true");
	IElementType KW_FALSE = new LapgElementType("false");
	IElementType KW_PRIO = new LapgElementType("prio");
	IElementType KW_SHIFT = new LapgElementType("shift");
	IElementType KW_REDUCE = new LapgElementType("reduce");

	TokenSet keywords = TokenSet.create(KW_TRUE, KW_FALSE, KW_PRIO, KW_SHIFT, KW_REDUCE);
}
