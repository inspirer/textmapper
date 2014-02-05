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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.textmapper.tool.parser.TMLexer.Lexems;

public interface TMTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	IElementType COMMENT = new TMElementType(Lexems._skip_comment, "comment");
	TokenSet comments = TokenSet.create(COMMENT);

	// tokens
	IElementType STRING = new TMElementType(Lexems.scon, "string");
	IElementType ICON = new TMElementType(Lexems.icon, "int");
	IElementType ID = new TMElementType(Lexems.ID, "ID");
	IElementType REGEXP = new TMElementType(Lexems.regexp, "regexp");

	TokenSet strings = TokenSet.create(STRING);

	// inner tokens
	IElementType TOKEN_ACTION = new TMTemplatesElementType(Lexems.code, true, "action");
	IElementType TEMPLATES = new TMTemplatesElementType(Lexems.eoi, false, "templates");

	// [] () {}
	IElementType OP_LBRACKET = new TMElementType(Lexems.Lsquare, "[");
	IElementType OP_RBRACKET = new TMElementType(Lexems.Rsquare, "]");
	IElementType OP_LPAREN = new TMElementType(Lexems.Lparen, "(");
	IElementType OP_RPAREN = new TMElementType(Lexems.Rparen, ")");
	IElementType OP_LCURLY = new TMElementType(Lexems.Lcurly, "{");
	IElementType OP_RCURLY = new TMElementType(Lexems.Rcurly, "}");

	// punctuation
	IElementType OP_SEMICOLON = new TMElementType(Lexems.Semicolon, ";");
	IElementType OP_DOT = new TMElementType(Lexems.Dot, ".");
	IElementType OP_COMMA = new TMElementType(Lexems.Comma, ",");
	IElementType OP_COLONCOLON = new TMElementType(Lexems.ColonColon, "::");

	// operators
	IElementType OP_PERCENT = new TMElementType(Lexems.Percent, "%");
	IElementType OP_DOLLAR = new TMElementType(Lexems.Dollar, "$");
	IElementType OP_DOTDOT = new TMElementType(Lexems.DotDot, "..");
	IElementType OP_CCEQ = new TMElementType(Lexems.ColonColonEqual, "::=");
	IElementType OP_OR = new TMElementType(Lexems.Or, "|");
	IElementType OP_EQ = new TMElementType(Lexems.Equal, "=");
	IElementType OP_EQGT = new TMElementType(Lexems.EqualGreater, "=>");
	IElementType OP_COLON = new TMElementType(Lexems.Colon, ":");
	IElementType OP_LT = new TMElementType(Lexems.Less, "<");
	IElementType OP_GT = new TMElementType(Lexems.Greater, ">");
	IElementType OP_STAR = new TMElementType(Lexems.Mult, "*");
	IElementType OP_PLUS = new TMElementType(Lexems.Plus, "+");
	IElementType OP_PLUSEQ = new TMElementType(Lexems.PlusEqual, "+=");
	IElementType OP_QMARK = new TMElementType(Lexems.Questionmark, "?");
	//TODO IElementType OP_ARROW = new TMElementType(Lexems.MINUSGREATER, "->");
	IElementType OP_AND = new TMElementType(Lexems.Ampersand, "&");
	IElementType OP_AT = new TMElementType(Lexems.Atsign, "@");

	TokenSet operators = TokenSet.create(
			OP_PERCENT, OP_DOLLAR, OP_DOTDOT, OP_CCEQ, OP_OR, OP_EQ, OP_EQGT, OP_COLON,
			OP_LT, OP_GT, OP_PLUSEQ/*, OP_ARROW*/, OP_AND, OP_AT
	);

	TokenSet quantifiers = TokenSet.create(OP_PLUS, OP_QMARK, OP_STAR);

	// keywords
	IElementType KW_TRUE = new TMElementType(Lexems.Ltrue, "true");
	IElementType KW_FALSE = new TMElementType(Lexems.Lfalse, "false");
	IElementType KW_NEW = new TMElementType(Lexems.Lnew, "new");
	IElementType KW_SEPARATOR = new TMElementType(Lexems.Lseparator, "separator");
	IElementType KW_AS = new TMElementType(Lexems.Las, "as");
	IElementType KW_IMPORT = new TMElementType(Lexems.Limport, "import");

	// soft keywords
	IElementType KW_PRIO = new TMElementType(Lexems.Lprio, "prio");
	IElementType KW_SHIFT = new TMElementType(Lexems.Lshift, "shift");
	IElementType KW_REDUCE = new TMElementType(Lexems.Lreduce, "reduce");
	IElementType KW_INPUT = new TMElementType(Lexems.Linput, "input");
	IElementType KW_LEFT = new TMElementType(Lexems.Lleft, "left");
	IElementType KW_RIGHT = new TMElementType(Lexems.Lright, "right");
	IElementType KW_NONASSOC = new TMElementType(Lexems.Lnonassoc, "nonassoc");
	IElementType KW_NOEOI = new TMElementType(Lexems.Lnoeoi, "no-eoi");
	IElementType KW_INLINE = new TMElementType(Lexems.Linline, "inline");
	IElementType KW_RETURNS = new TMElementType(Lexems.Lreturns, "returns");
	IElementType KW_INTERFACE = new TMElementType(Lexems.Linterface, "interface");
	IElementType KW_VOID = new TMElementType(Lexems.Lvoid, "void");
	IElementType KW_LANGUAGE = new TMElementType(Lexems.Llanguage, "language");
	IElementType KW_LALR = new TMElementType(Lexems.Llalr, "lalr");
	IElementType KW_LEXER = new TMElementType(Lexems.Llexer, "lexer");
	IElementType KW_LEXER_ACC = new TMElementType(Lexems.Llexer, "lexer");
	IElementType KW_PARSER = new TMElementType(Lexems.Lparser, "parser");
	IElementType KW_PARSER_ACC = new TMElementType(Lexems.Lparser, "parser");
	IElementType KW_SOFT = new TMElementType(Lexems.Lsoft, "soft");
	IElementType KW_CLASS = new TMElementType(Lexems.Lclass, "class");
	IElementType KW_SPACE = new TMElementType(Lexems.Lspace, "space");

	TokenSet keywords = TokenSet.create(
			KW_TRUE, KW_FALSE, KW_NEW, KW_SEPARATOR, KW_AS, KW_IMPORT);

	TokenSet softKeywords = TokenSet.create(
			KW_PRIO, KW_SHIFT, KW_REDUCE,
			KW_INPUT, KW_LEFT, KW_RIGHT, KW_NONASSOC, KW_NOEOI,
			KW_INLINE, KW_RETURNS, KW_INTERFACE, KW_VOID, KW_LANGUAGE,
			KW_LALR, KW_LEXER, KW_PARSER, KW_SOFT, KW_CLASS, KW_SPACE);
}
