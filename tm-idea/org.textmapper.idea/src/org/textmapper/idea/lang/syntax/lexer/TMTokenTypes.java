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
import org.textmapper.tool.parser.TMLexer.Tokens;

public interface TMTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	IElementType COMMENT = new TMElementType(Tokens._skip_comment, "comment");
	TokenSet comments = TokenSet.create(COMMENT);

	// tokens
	IElementType STRING = new TMElementType(Tokens.scon, "string");
	IElementType ICON = new TMElementType(Tokens.icon, "int");
	IElementType ID = new TMElementType(Tokens.ID, "ID");
	IElementType REGEXP = new TMElementType(Tokens.regexp, "regexp");

	TokenSet strings = TokenSet.create(STRING);

	// inner tokens
	IElementType TOKEN_ACTION = new TMTemplatesElementType(Tokens.code, true, "action");
	IElementType TEMPLATES = new TMTemplatesElementType(Tokens.eoi, false, "templates");

	// [] () {}
	IElementType OP_LBRACKET = new TMElementType(Tokens.Lsquare, "[");
	IElementType OP_RBRACKET = new TMElementType(Tokens.Rsquare, "]");
	IElementType OP_LPAREN = new TMElementType(Tokens.Lparen, "(");
	IElementType OP_RPAREN = new TMElementType(Tokens.Rparen, ")");
	IElementType OP_LCURLY = new TMElementType(Tokens.Lcurly, "{");
	IElementType OP_LCURLYTILDE = new TMElementType(Tokens.LcurlyTilde, "{~");
	IElementType OP_RCURLY = new TMElementType(Tokens.Rcurly, "}");

	// punctuation
	IElementType OP_SEMICOLON = new TMElementType(Tokens.Semicolon, ";");
	IElementType OP_DOT = new TMElementType(Tokens.Dot, ".");
	IElementType OP_COMMA = new TMElementType(Tokens.Comma, ",");
	IElementType OP_COLONCOLON = new TMElementType(Tokens.ColonColon, "::");

	// operators
	IElementType OP_PERCENT = new TMElementType(Tokens.Percent, "%");
	IElementType OP_DOLLAR = new TMElementType(Tokens.Dollar, "$");
	IElementType OP_CCEQ = new TMElementType(Tokens.ColonColonEqual, "::=");
	IElementType OP_OR = new TMElementType(Tokens.Or, "|");
	IElementType OP_OROR = new TMElementType(Tokens.OrOr, "||");
	IElementType OP_EQ = new TMElementType(Tokens.Equal, "=");
	IElementType OP_EQEQ = new TMElementType(Tokens.EqualEqual, "==");
	IElementType OP_EXCLEQ = new TMElementType(Tokens.ExclamationEqual, "!=");
	IElementType OP_EQGT = new TMElementType(Tokens.EqualGreater, "=>");
	IElementType OP_COLON = new TMElementType(Tokens.Colon, ":");
	IElementType OP_LT = new TMElementType(Tokens.Less, "<");
	IElementType OP_GT = new TMElementType(Tokens.Greater, ">");
	IElementType OP_STAR = new TMElementType(Tokens.Mult, "*");
	IElementType OP_PLUS = new TMElementType(Tokens.Plus, "+");
	IElementType OP_PLUSEQ = new TMElementType(Tokens.PlusEqual, "+=");
	IElementType OP_QMARK = new TMElementType(Tokens.Questionmark, "?");
	IElementType OP_EMARK = new TMElementType(Tokens.Exclamation, "!");
	//TODO IElementType OP_ARROW = new TMElementType(Tokens.MINUSGREATER, "->");
	IElementType OP_AND = new TMElementType(Tokens.Ampersand, "&");
	IElementType OP_ANDAND = new TMElementType(Tokens.AmpersandAmpersand, "&&");
	IElementType OP_AT = new TMElementType(Tokens.Atsign, "@");
	IElementType OP_TILDE = new TMElementType(Tokens.Tilde, "~");

	TokenSet operators = TokenSet.create(
			OP_PERCENT, OP_DOLLAR, OP_CCEQ, OP_OR, OP_OROR, OP_EQ, OP_EQEQ, OP_EXCLEQ, OP_EQGT, OP_COLON,
			OP_LT, OP_GT, OP_PLUSEQ, OP_EMARK/*, OP_ARROW*/, OP_AND, OP_ANDAND, OP_AT, OP_TILDE
	);

	TokenSet quantifiers = TokenSet.create(OP_PLUS, OP_QMARK, OP_STAR);

	// keywords
	IElementType KW_TRUE = new TMElementType(Tokens.Ltrue, "true");
	IElementType KW_FALSE = new TMElementType(Tokens.Lfalse, "false");
	IElementType KW_NEW = new TMElementType(Tokens.Lnew, "new");
	IElementType KW_SEPARATOR = new TMElementType(Tokens.Lseparator, "separator");
	IElementType KW_AS = new TMElementType(Tokens.Las, "as");
	IElementType KW_IMPORT = new TMElementType(Tokens.Limport, "import");
	IElementType KW_SET = new TMElementType(Tokens.Lset, "set");

	// soft keywords
	IElementType KW_BRACKETS = new TMElementType(Tokens.Lbrackets, "brackets");
	IElementType KW_INLINE = new TMElementType(Tokens.Linline, "inline");

	IElementType KW_PRIO = new TMElementType(Tokens.Lprio, "prio");
	IElementType KW_SHIFT = new TMElementType(Tokens.Lshift, "shift");
	IElementType KW_REDUCE = new TMElementType(Tokens.Lreduce, "reduce");

	IElementType KW_RETURNS = new TMElementType(Tokens.Lreturns, "returns");

	IElementType KW_INPUT = new TMElementType(Tokens.Linput, "input");
	IElementType KW_LEFT = new TMElementType(Tokens.Lleft, "left");
	IElementType KW_RIGHT = new TMElementType(Tokens.Lright, "right");
	IElementType KW_NONASSOC = new TMElementType(Tokens.Lnonassoc, "nonassoc");

	IElementType KW_GENERATE = new TMElementType(Tokens.Lgenerate, "generate");
	IElementType KW_ASSERT = new TMElementType(Tokens.Lassert, "assert");
	IElementType KW_EMPTY = new TMElementType(Tokens.Lempty, "empty");
	IElementType KW_NONEMPTY = new TMElementType(Tokens.Lnonempty, "nonempty");

	IElementType KW_PARAM = new TMElementType(Tokens.Lparam, "param");
	IElementType KW_STRING = new TMElementType(Tokens.Lstring, "string");
	IElementType KW_BOOL = new TMElementType(Tokens.Lbool, "bool");
	IElementType KW_INT = new TMElementType(Tokens.Lint, "int");
	IElementType KW_SYMBOL = new TMElementType(Tokens.Lsymbol, "symbol");

	IElementType KW_NOEOI = new TMElementType(Tokens.Lnoeoi, "no-eoi");

	IElementType KW_SOFT = new TMElementType(Tokens.Lsoft, "soft");
	IElementType KW_CLASS = new TMElementType(Tokens.Lclass, "class");
	IElementType KW_INTERFACE = new TMElementType(Tokens.Linterface, "interface");
	IElementType KW_VOID = new TMElementType(Tokens.Lvoid, "void");
	IElementType KW_SPACE = new TMElementType(Tokens.Lspace, "space");
	IElementType KW_LAYOUT = new TMElementType(Tokens.Llayout, "layout");
	IElementType KW_LANGUAGE = new TMElementType(Tokens.Llanguage, "language");
	IElementType KW_LALR = new TMElementType(Tokens.Llalr, "lalr");

	IElementType KW_LEXER = new TMElementType(Tokens.Llexer, "lexer");
	IElementType KW_LEXER_ACC = new TMElementType(Tokens.Llexer, "lexer");
	IElementType KW_PARSER = new TMElementType(Tokens.Lparser, "parser");
	IElementType KW_PARSER_ACC = new TMElementType(Tokens.Lparser, "parser");

	TokenSet keywords = TokenSet.create(
			KW_TRUE, KW_FALSE, KW_NEW, KW_SEPARATOR, KW_AS, KW_IMPORT, KW_SET);

	TokenSet softKeywords = TokenSet.create(
			KW_BRACKETS, KW_INLINE,
			KW_PRIO, KW_SHIFT, KW_REDUCE,
			KW_RETURNS,
			KW_INPUT, KW_LEFT, KW_RIGHT, KW_NONASSOC,
			KW_GENERATE, KW_ASSERT, KW_EMPTY, KW_NONEMPTY,
			KW_PARAM, KW_STRING, KW_BOOL, KW_INT, KW_SYMBOL,
			KW_NOEOI,
			KW_SOFT, KW_CLASS, KW_INTERFACE, KW_VOID, KW_SPACE, KW_LAYOUT, KW_LANGUAGE, KW_LALR,
			KW_LEXER, KW_PARSER);
}
