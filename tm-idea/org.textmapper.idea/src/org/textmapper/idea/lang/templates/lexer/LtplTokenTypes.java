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
import org.textmapper.templates.ast.TemplatesLexer.Lexems;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public interface LtplTokenTypes {

	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespaces = TokenSet.create(WHITESPACE);

	// comments
	TokenSet comments = TokenSet.create();

	// tokens
	IElementType IDENTIFIER = new LtplElementType(Lexems.identifier, "identifier");
	IElementType CCON = new LtplElementType(Lexems.ccon, "string");
	IElementType ICON = new LtplElementType(Lexems.icon, "int");
	IElementType ESCID = new LtplElementType(Lexems.escid, "$identifier");
	IElementType ESCINT = new LtplElementType(Lexems.escint, "$number");
	IElementType ANY = new LtplElementType(Lexems.any, "any");

	TokenSet strings = TokenSet.create(CCON);

	// [] ()
	IElementType OP_LSQUARE = new LtplElementType(Lexems.Lsquare, "[");
	IElementType OP_RSQUARE = new LtplElementType(Lexems.Rsquare, "]");
	IElementType OP_LPAREN = new LtplElementType(Lexems.Lparen, "(");
	IElementType OP_RPAREN = new LtplElementType(Lexems.Rparen, ")");
	IElementType OP_LCURLY = new LtplElementType(Lexems.Lcurly, "{");
	IElementType OP_RCURLY = new LtplElementType(Lexems.Rcurly, "}");

	// punctuation
	IElementType OP_DOT = new LtplElementType(Lexems.Dot, ".");
	IElementType OP_COMMA = new LtplElementType(Lexems.Comma, ",");

	// operators
	IElementType OP_ESCDOLLAR = new LtplElementType(Lexems.escdollar, "$$");
	IElementType OP_DOLLARLCURLY = new LtplElementType(Lexems.DollarLcurly, "${");
	IElementType OP_MINUSRCURLY = new LtplElementType(Lexems.MinusRcurly, "-}");
	IElementType OP_DOLLARSLASH = new LtplElementType(Lexems.DollarSlash, "$/");
	IElementType OP_PLUS = new LtplElementType(Lexems.Plus, "+");
	IElementType OP_MINUS = new LtplElementType(Lexems.Minus, "-");
	IElementType OP_MULT = new LtplElementType(Lexems.Mult, "*");
	IElementType OP_SLASH = new LtplElementType(Lexems.Slash, "/");
	IElementType OP_PERCENT = new LtplElementType(Lexems.Percent, "%");
	IElementType OP_EXCLAMATION = new LtplElementType(Lexems.Exclamation, "!");
	IElementType OP_OR = new LtplElementType(Lexems.Or, "|");
	IElementType OP_AMPERSANDAMPERSAND = new LtplElementType(Lexems.AmpersandAmpersand, "&&");
	IElementType OP_OROR = new LtplElementType(Lexems.OrOr, "||");
	IElementType OP_EQUALEQUAL = new LtplElementType(Lexems.EqualEqual, "==");
	IElementType OP_EQUAL = new LtplElementType(Lexems.Equal, "=");
	IElementType OP_EXCLAMATIONEQUAL = new LtplElementType(Lexems.ExclamationEqual, "!=");
	IElementType OP_MINUSGREATER = new LtplElementType(Lexems.MinusGreater, "->");
	IElementType OP_EQUALGREATER = new LtplElementType(Lexems.EqualGreater, "=>");
	IElementType OP_LESSEQUAL = new LtplElementType(Lexems.LessEqual, "<=");
	IElementType OP_GREATEREQUAL = new LtplElementType(Lexems.GreaterEqual, ">=");
	IElementType OP_LESS = new LtplElementType(Lexems.Less, "<");
	IElementType OP_GREATER = new LtplElementType(Lexems.Greater, ">");
	IElementType OP_COLON = new LtplElementType(Lexems.Colon, ":");
	IElementType OP_QUESTIONMARK = new LtplElementType(Lexems.Questionmark, "?");

	TokenSet operators = TokenSet.create(
			OP_DOLLARLCURLY, OP_DOLLARSLASH, OP_MINUSRCURLY, OP_PLUS, OP_MINUS, OP_MULT,
			OP_SLASH, OP_PERCENT, OP_EXCLAMATION, OP_OR, OP_AMPERSANDAMPERSAND, OP_OROR,
			OP_EQUALEQUAL, OP_EQUAL, OP_EXCLAMATIONEQUAL, OP_MINUSGREATER, OP_EQUALGREATER,
			OP_LESSEQUAL, OP_GREATEREQUAL, OP_LESS, OP_GREATER, OP_COLON, OP_QUESTIONMARK,
			OP_ESCDOLLAR);

	// keywords
	IElementType KW_CALL = new LtplElementType(Lexems.Lcall, "call");
	IElementType KW_CACHED = new LtplElementType(Lexems.Lcached, "cached");
	IElementType KW_CASE = new LtplElementType(Lexems.Lcase, "case");
	IElementType KW_END = new LtplElementType(Lexems.Lend, "end");
	IElementType KW_ELSE = new LtplElementType(Lexems.Lelse, "else");
	IElementType KW_EVAL = new LtplElementType(Lexems.Leval, "eval");
	IElementType KW_FALSE = new LtplElementType(Lexems.Lfalse, "false");
	IElementType KW_FOR = new LtplElementType(Lexems.Lfor, "for");
	IElementType KW_FILE = new LtplElementType(Lexems.Lfile, "file");
	IElementType KW_FOREACH = new LtplElementType(Lexems.Lforeach, "foreach");
	IElementType KW_GREP = new LtplElementType(Lexems.Lgrep, "grep");
	IElementType KW_IF = new LtplElementType(Lexems.Lif, "if");
	IElementType KW_IN = new LtplElementType(Lexems.Lin, "in");
	IElementType KW_IMPORT = new LtplElementType(Lexems.Limport, "import");
	IElementType KW_IS = new LtplElementType(Lexems.Lis, "is");
	IElementType KW_MAP = new LtplElementType(Lexems.Lmap, "map");
	IElementType KW_NEW = new LtplElementType(Lexems.Lnew, "new");
	IElementType KW_NULL = new LtplElementType(Lexems.Lnull, "null");
	IElementType KW_QUERY = new LtplElementType(Lexems.Lquery, "query");
	IElementType KW_SWITCH = new LtplElementType(Lexems.Lswitch, "switch");
	IElementType KW_SEPARATOR = new LtplElementType(Lexems.Lseparator, "separator");
	IElementType KW_TEMPLATE = new LtplElementType(Lexems.Ltemplate, "template");
	IElementType KW_TRUE = new LtplElementType(Lexems.Ltrue, "true");
	IElementType KW_SELF = new LtplElementType(Lexems.Lself, "self");
	IElementType KW_ASSERT = new LtplElementType(Lexems.Lassert, "assert");


	TokenSet keywords = TokenSet.create(KW_CALL, KW_CACHED, KW_CASE, KW_END, KW_ELSE, KW_EVAL,
			KW_FALSE, KW_FOR, KW_FILE, KW_FOREACH, KW_GREP, KW_IF, KW_IN, KW_IMPORT, KW_IS,
			KW_MAP, KW_NEW, KW_NULL, KW_QUERY, KW_SWITCH, KW_SEPARATOR, KW_TEMPLATE,
			KW_TRUE, KW_SELF, KW_ASSERT);
}
