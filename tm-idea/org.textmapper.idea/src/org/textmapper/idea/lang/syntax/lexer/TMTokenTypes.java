/**
 * Copyright 2010-2017 Evgeny Gryaznov
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
	IElementType ML_COMMENT = new TMElementType(Tokens._skip_multiline, "ml-comment");
	TokenSet comments = TokenSet.create(COMMENT, ML_COMMENT);

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
	IElementType OP_LBRACKET = new TMElementType(Tokens.Lbrack, "[");
	IElementType OP_RBRACKET = new TMElementType(Tokens.Rbrack, "]");
	IElementType OP_LPAREN = new TMElementType(Tokens.Lparen, "(");
	IElementType OP_LPAREN_QA = new TMElementType(Tokens.LparenQuestAssign, "(?=");
	IElementType OP_RPAREN = new TMElementType(Tokens.Rparen, ")");
	IElementType OP_LCURLY = new TMElementType(Tokens.Lbrace, "{");
	IElementType OP_RCURLY = new TMElementType(Tokens.Rbrace, "}");

	// punctuation
	IElementType OP_SEMICOLON = new TMElementType(Tokens.Semicolon, ";");
	IElementType OP_DOT = new TMElementType(Tokens.Dot, ".");
	IElementType OP_COMMA = new TMElementType(Tokens.Comma, ",");
	IElementType OP_COLONCOLON = new TMElementType(Tokens.ColonColon, "::");

	// operators
	IElementType OP_PERCENT = new TMElementType(Tokens.Rem, "%");
	IElementType OP_DOLLAR = new TMElementType(Tokens.Dollar, "$");
	IElementType OP_OR = new TMElementType(Tokens.Or, "|");
	IElementType OP_OROR = new TMElementType(Tokens.OrOr, "||");
	IElementType OP_EQ = new TMElementType(Tokens.Assign, "=");
	IElementType OP_EQEQ = new TMElementType(Tokens.AssignAssign, "==");
	IElementType OP_EXCLEQ = new TMElementType(Tokens.ExclAssign, "!=");
	IElementType OP_COLON = new TMElementType(Tokens.Colon, ":");
	IElementType OP_LT = new TMElementType(Tokens.Lt, "<");
	IElementType OP_GT = new TMElementType(Tokens.Gt, ">");
	IElementType OP_STAR = new TMElementType(Tokens.Mult, "*");
	IElementType OP_PLUS = new TMElementType(Tokens.Plus, "+");
	IElementType OP_PLUSEQ = new TMElementType(Tokens.PlusAssign, "+=");
	IElementType OP_QMARK = new TMElementType(Tokens.Quest, "?");
	IElementType OP_EMARK = new TMElementType(Tokens.Excl, "!");
	IElementType OP_ARROW = new TMElementType(Tokens.MinusGt, "->");
	IElementType OP_AND = new TMElementType(Tokens.And, "&");
	IElementType OP_ANDAND = new TMElementType(Tokens.AndAnd, "&&");
	IElementType OP_AT = new TMElementType(Tokens.Atsign, "@");
	IElementType OP_TILDE = new TMElementType(Tokens.Tilde, "~");
	IElementType OP_DIV = new TMElementType(Tokens.Div, "/");

	TokenSet operators = TokenSet.create(
			OP_PERCENT, OP_DOLLAR, OP_OR, OP_OROR, OP_EQ, OP_EQEQ, OP_EXCLEQ, OP_COLON,
			OP_LT, OP_GT, OP_PLUSEQ, OP_EMARK, OP_ARROW, OP_AND, OP_ANDAND, OP_AT, OP_TILDE, OP_DIV
	);

	TokenSet quantifiers = TokenSet.create(OP_PLUS, OP_QMARK, OP_STAR);

	// keywords
	IElementType KW_TRUE = new TMElementType(Tokens._true, "true");
	IElementType KW_FALSE = new TMElementType(Tokens._false, "false");
	IElementType KW_SEPARATOR = new TMElementType(Tokens.separator, "separator");
	IElementType KW_AS = new TMElementType(Tokens.as, "as");
	IElementType KW_IMPORT = new TMElementType(Tokens._import, "import");
	IElementType KW_SET = new TMElementType(Tokens.set, "set");
	IElementType KW_IMPLEMENTS = new TMElementType(Tokens._implements, "implements");

	// soft keywords
	IElementType KW_BRACKETS = new TMElementType(Tokens.brackets, "brackets");
	IElementType KW_S = new TMElementType(Tokens.char_s, "s");
	IElementType KW_X = new TMElementType(Tokens.char_x, "x");
	IElementType KW_INLINE = new TMElementType(Tokens.inline, "inline");

	IElementType KW_PREC = new TMElementType(Tokens.prec, "prec");
	IElementType KW_SHIFT = new TMElementType(Tokens.shift, "shift");

	IElementType KW_RETURNS = new TMElementType(Tokens.returns, "returns");

	IElementType KW_INPUT = new TMElementType(Tokens.input, "input");
	IElementType KW_LEFT = new TMElementType(Tokens.left, "left");
	IElementType KW_RIGHT = new TMElementType(Tokens.right, "right");
	IElementType KW_NONASSOC = new TMElementType(Tokens.nonassoc, "nonassoc");

	IElementType KW_GENERATE = new TMElementType(Tokens.generate, "generate");
	IElementType KW_ASSERT = new TMElementType(Tokens._assert, "assert");
	IElementType KW_EMPTY = new TMElementType(Tokens.empty, "empty");
	IElementType KW_NONEMPTY = new TMElementType(Tokens.nonempty, "nonempty");

	IElementType KW_EXPLICIT = new TMElementType(Tokens.explicit, "explicit");
	IElementType KW_GLOBAL = new TMElementType(Tokens.global, "global");
	IElementType KW_LOOKAHEAD = new TMElementType(Tokens.lookahead, "lookahead");
	IElementType KW_PARAM = new TMElementType(Tokens.param, "param");
	IElementType KW_FLAG = new TMElementType(Tokens.flag, "flag");

	IElementType KW_NOEOI = new TMElementType(Tokens.noMinuseoi, "no-eoi");

	IElementType KW_CLASS = new TMElementType(Tokens._class, "class");
	IElementType KW_INTERFACE = new TMElementType(Tokens._interface, "interface");
	IElementType KW_VOID = new TMElementType(Tokens._void, "void");
	IElementType KW_SPACE = new TMElementType(Tokens.space, "space");
	IElementType KW_LAYOUT = new TMElementType(Tokens.layout, "layout");
	IElementType KW_LANGUAGE = new TMElementType(Tokens.language, "language");
	IElementType KW_LALR = new TMElementType(Tokens.lalr, "lalr");

	IElementType KW_LEXER = new TMElementType(Tokens.lexer, "lexer");
	IElementType KW_LEXER_ACC = new TMElementType(Tokens.lexer, "lexer");
	IElementType KW_PARSER = new TMElementType(Tokens.parser, "parser");
	IElementType KW_PARSER_ACC = new TMElementType(Tokens.parser, "parser");

	TokenSet keywords = TokenSet.create(
			KW_TRUE, KW_FALSE, KW_SEPARATOR, KW_AS, KW_IMPORT, KW_SET, KW_IMPLEMENTS);

	TokenSet softKeywords = TokenSet.create(
			KW_BRACKETS, KW_INLINE, KW_S, KW_X,
			KW_PREC, KW_SHIFT,
			KW_RETURNS,
			KW_INPUT, KW_LEFT, KW_RIGHT, KW_NONASSOC,
			KW_GENERATE, KW_ASSERT, KW_EMPTY, KW_NONEMPTY,
			KW_EXPLICIT, KW_GLOBAL, KW_LOOKAHEAD, KW_PARAM, KW_FLAG,
			KW_NOEOI,
			KW_CLASS, KW_INTERFACE, KW_VOID, KW_SPACE, KW_LAYOUT, KW_LANGUAGE, KW_LALR,
			KW_LEXER, KW_PARSER);
}
