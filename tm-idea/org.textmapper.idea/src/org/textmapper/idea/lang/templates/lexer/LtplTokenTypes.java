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
package org.textmapper.idea.lang.templates.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.textmapper.templates.ast.TemplatesLexer.Tokens;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public interface LtplTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	// comments
	TokenSet comments = TokenSet.create();

	// tokens
	IElementType IDENTIFIER = new LtplElementType(Tokens.identifier, "identifier");
	IElementType CCON = new LtplElementType(Tokens.ccon, "string");
	IElementType ICON = new LtplElementType(Tokens.icon, "int");
	IElementType ESCID = new LtplElementType(Tokens.escid, "$identifier");
	IElementType ESCINT = new LtplElementType(Tokens.escint, "$number");
	IElementType ANY = new LtplElementType(Tokens.any, "any");

	TokenSet strings = TokenSet.create(CCON);

	// [] ()
	IElementType OP_LSQUARE = new LtplElementType(Tokens.Lsquare, "[");
	IElementType OP_RSQUARE = new LtplElementType(Tokens.Rsquare, "]");
	IElementType OP_LPAREN = new LtplElementType(Tokens.Lparen, "(");
	IElementType OP_RPAREN = new LtplElementType(Tokens.Rparen, ")");
	IElementType OP_LCURLY = new LtplElementType(Tokens.Lcurly, "{");
	IElementType OP_RCURLY = new LtplElementType(Tokens.Rcurly, "}");

	// punctuation
	IElementType OP_DOT = new LtplElementType(Tokens.Dot, ".");
	IElementType OP_COMMA = new LtplElementType(Tokens.Comma, ",");

	// operators
	IElementType OP_ESCDOLLAR = new LtplElementType(Tokens.escdollar, "$$");
	IElementType OP_DOLLARLCURLY = new LtplElementType(Tokens.DollarLcurly, "${");
	IElementType OP_MINUSRCURLY = new LtplElementType(Tokens.MinusRcurly, "-}");
	IElementType OP_DOLLARSLASH = new LtplElementType(Tokens.DollarSlash, "$/");
	IElementType OP_PLUS = new LtplElementType(Tokens.Plus, "+");
	IElementType OP_MINUS = new LtplElementType(Tokens.Minus, "-");
	IElementType OP_MULT = new LtplElementType(Tokens.Mult, "*");
	IElementType OP_SLASH = new LtplElementType(Tokens.Slash, "/");
	IElementType OP_PERCENT = new LtplElementType(Tokens.Percent, "%");
	IElementType OP_EXCLAMATION = new LtplElementType(Tokens.Exclamation, "!");
	IElementType OP_OR = new LtplElementType(Tokens.Or, "|");
	IElementType OP_AMPERSANDAMPERSAND = new LtplElementType(Tokens.AmpersandAmpersand, "&&");
	IElementType OP_OROR = new LtplElementType(Tokens.OrOr, "||");
	IElementType OP_EQUALEQUAL = new LtplElementType(Tokens.EqualEqual, "==");
	IElementType OP_EQUAL = new LtplElementType(Tokens.Equal, "=");
	IElementType OP_EXCLAMATIONEQUAL = new LtplElementType(Tokens.ExclamationEqual, "!=");
	IElementType OP_MINUSGREATER = new LtplElementType(Tokens.MinusGreater, "->");
	IElementType OP_EQUALGREATER = new LtplElementType(Tokens.EqualGreater, "=>");
	IElementType OP_LESSEQUAL = new LtplElementType(Tokens.LessEqual, "<=");
	IElementType OP_GREATEREQUAL = new LtplElementType(Tokens.GreaterEqual, ">=");
	IElementType OP_LESS = new LtplElementType(Tokens.Less, "<");
	IElementType OP_GREATER = new LtplElementType(Tokens.Greater, ">");
	IElementType OP_COLON = new LtplElementType(Tokens.Colon, ":");
	IElementType OP_QUESTIONMARK = new LtplElementType(Tokens.Questionmark, "?");

	TokenSet operators = TokenSet.create(
			OP_DOLLARLCURLY, OP_DOLLARSLASH, OP_MINUSRCURLY, OP_PLUS, OP_MINUS, OP_MULT,
			OP_SLASH, OP_PERCENT, OP_EXCLAMATION, OP_OR, OP_AMPERSANDAMPERSAND, OP_OROR,
			OP_EQUALEQUAL, OP_EQUAL, OP_EXCLAMATIONEQUAL, OP_MINUSGREATER, OP_EQUALGREATER,
			OP_LESSEQUAL, OP_GREATEREQUAL, OP_LESS, OP_GREATER, OP_COLON, OP_QUESTIONMARK,
			OP_ESCDOLLAR);

	// keywords
	IElementType KW_CALL = new LtplElementType(Tokens.Lcall, "call");
	IElementType KW_CACHED = new LtplElementType(Tokens.Lcached, "cached");
	IElementType KW_CASE = new LtplElementType(Tokens.Lcase, "case");
	IElementType KW_END = new LtplElementType(Tokens.Lend, "end");
	IElementType KW_ELSE = new LtplElementType(Tokens.Lelse, "else");
	IElementType KW_EVAL = new LtplElementType(Tokens.Leval, "eval");
	IElementType KW_FALSE = new LtplElementType(Tokens.Lfalse, "false");
	IElementType KW_FOR = new LtplElementType(Tokens.Lfor, "for");
	IElementType KW_FILE = new LtplElementType(Tokens.Lfile, "file");
	IElementType KW_FOREACH = new LtplElementType(Tokens.Lforeach, "foreach");
	IElementType KW_GREP = new LtplElementType(Tokens.Lgrep, "grep");
	IElementType KW_IF = new LtplElementType(Tokens.Lif, "if");
	IElementType KW_IN = new LtplElementType(Tokens.Lin, "in");
	IElementType KW_IMPORT = new LtplElementType(Tokens.Limport, "import");
	IElementType KW_IS = new LtplElementType(Tokens.Lis, "is");
	IElementType KW_MAP = new LtplElementType(Tokens.Lmap, "map");
	IElementType KW_NEW = new LtplElementType(Tokens.Lnew, "new");
	IElementType KW_NULL = new LtplElementType(Tokens.Lnull, "null");
	IElementType KW_QUERY = new LtplElementType(Tokens.Lquery, "query");
	IElementType KW_SWITCH = new LtplElementType(Tokens.Lswitch, "switch");
	IElementType KW_SEPARATOR = new LtplElementType(Tokens.Lseparator, "separator");
	IElementType KW_TEMPLATE = new LtplElementType(Tokens.Ltemplate, "template");
	IElementType KW_TRUE = new LtplElementType(Tokens.Ltrue, "true");
	IElementType KW_SELF = new LtplElementType(Tokens.Lself, "self");
	IElementType KW_ASSERT = new LtplElementType(Tokens.Lassert, "assert");


	TokenSet keywords = TokenSet.create(KW_CALL, KW_CACHED, KW_CASE, KW_END, KW_ELSE, KW_EVAL,
			KW_FALSE, KW_FOR, KW_FILE, KW_FOREACH, KW_GREP, KW_IF, KW_IN, KW_IMPORT, KW_IS,
			KW_MAP, KW_NEW, KW_NULL, KW_QUERY, KW_SWITCH, KW_SEPARATOR, KW_TEMPLATE,
			KW_TRUE, KW_SELF, KW_ASSERT);
}
