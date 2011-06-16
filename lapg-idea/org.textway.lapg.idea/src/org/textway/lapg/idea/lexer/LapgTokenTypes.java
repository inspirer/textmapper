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
package org.textway.lapg.idea.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.textway.lapg.parser.LapgLexer.Lexems;
import org.textway.lapg.parser.LapgParser.Tokens;

public interface LapgTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	IElementType COMMENT = new LapgElementType(Lexems._skip_comment, "comment");
	TokenSet comments = TokenSet.create(COMMENT);

	// tokens
	IElementType STRING = new LapgElementType(Lexems.scon, "string");
	IElementType ICON = new LapgElementType(Lexems.icon, "int");
	IElementType IDENTIFIER = new LapgElementType(Lexems.identifier, "identifier");
	IElementType REGEXP = new LapgElementType(Lexems.regexp, "regexp");

	TokenSet strings = TokenSet.create(STRING);

	// inner tokens
	IElementType TOKEN_ACTION = new LapgElementType(Tokens.LCURLY, "action");
	IElementType TEMPLATES = new LapgElementType(Lexems.eoi, "templates");

	// [] ()
	IElementType OP_LBRACKET = new LapgElementType(Lexems.LSQUARE, "[");
	IElementType OP_RBRACKET = new LapgElementType(Lexems.RSQUARE, "]");
	IElementType OP_LPAREN = new LapgElementType(Lexems.LPAREN, "(");
	IElementType OP_RPAREN = new LapgElementType(Lexems.RPAREN, ")");

	// punctuation
	IElementType OP_SEMICOLON = new LapgElementType(Lexems.SEMICOLON, ";");
	IElementType OP_DOT = new LapgElementType(Lexems.DOT, ".");
	IElementType OP_COMMA = new LapgElementType(Lexems.COMMA, ",");

	// operators
	IElementType OP_PERCENT = new LapgElementType(Lexems.PERCENT, "%");
	IElementType OP_CCEQ = new LapgElementType(Lexems.COLONCOLONEQUAL, "::=");
	IElementType OP_OR = new LapgElementType(Lexems.OR, "|");
	IElementType OP_EQ = new LapgElementType(Lexems.EQUAL, "=");
	IElementType OP_EQGT = new LapgElementType(Lexems.EQUALGREATER, "=>");
	IElementType OP_COLON = new LapgElementType(Lexems.COLON, ":");
	IElementType OP_LT = new LapgElementType(Lexems.LESS, "<");
	IElementType OP_GT = new LapgElementType(Lexems.GREATER, ">");
	IElementType OP_STAR = new LapgElementType(Lexems.MULT, "*");
	IElementType OP_PLUS = new LapgElementType(Lexems.PLUS, "+");
	IElementType OP_QMARK = new LapgElementType(Lexems.QUESTIONMARK, "?");
	IElementType OP_AND = new LapgElementType(Lexems.AMPERSAND, "&");
	IElementType OP_AT = new LapgElementType(Lexems.ATSIGN, "@");

	TokenSet operators = TokenSet.create(
			OP_PERCENT, OP_CCEQ, OP_OR, OP_EQ, OP_EQGT, OP_COLON,
			OP_LT, OP_GT, OP_STAR, OP_PLUS, OP_QMARK, OP_AND, OP_AT
	);

	// keywords
	IElementType KW_TRUE = new LapgElementType(Lexems.Ltrue, "true");
	IElementType KW_FALSE = new LapgElementType(Lexems.Lfalse, "false");
	IElementType KW_PRIO = new LapgElementType(Lexems.Lprio, "prio");
	IElementType KW_SHIFT = new LapgElementType(Lexems.Lshift, "shift");
	IElementType KW_REDUCE = new LapgElementType(Lexems.Lreduce, "reduce");
	IElementType KW_INPUT = new LapgElementType(Lexems.Linput, "input");
	IElementType KW_LEFT = new LapgElementType(Lexems.Lleft, "left");
	IElementType KW_RIGHT = new LapgElementType(Lexems.Lright, "right");
	IElementType KW_NONASSOC = new LapgElementType(Lexems.Lnonassoc, "nonassoc");

	TokenSet keywords = TokenSet.create(KW_TRUE, KW_FALSE, KW_PRIO, KW_SHIFT, KW_REDUCE, KW_INPUT, KW_LEFT, KW_RIGHT, KW_NONASSOC);
}
