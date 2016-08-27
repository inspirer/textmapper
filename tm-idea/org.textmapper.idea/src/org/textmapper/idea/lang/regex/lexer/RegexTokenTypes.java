/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
package org.textmapper.idea.lang.regex.lexer;

import com.intellij.psi.tree.IElementType;
import org.textmapper.lapg.regex.RegexDefLexer.Tokens;

/**
 * evgeny, 3/4/12
 */
public interface RegexTokenTypes {

	IElementType RE_DELIMITERS = new RegexElementType(-1, "/");

	IElementType RE_CHAR = new RegexElementType(Tokens._char, "char");
	IElementType RE_ESCAPED = new RegexElementType(Tokens.escaped, "\\escaped");
	IElementType RE_CHARCLASS = new RegexElementType(Tokens.charclass, "charclass");

	IElementType RE_DOT = new RegexElementType(Tokens.Dot, ".");
	IElementType RE_MULT = new RegexElementType(Tokens.Mult, "*");
	IElementType RE_PLUS = new RegexElementType(Tokens.Plus, "+");
	IElementType RE_QUESTIONMARK = new RegexElementType(Tokens.Quest, "?");
	IElementType RE_QUANTFIER = new RegexElementType(Tokens.quantifier, "{n,m}");

	IElementType RE_LPAREN = new RegexElementType(Tokens.Lparen, "(");
	IElementType RE_LPARENQMARK = new RegexElementType(Tokens.LparenQuest, "(?");
	IElementType RE_OR = new RegexElementType(Tokens.Or, "|");
	IElementType RE_RPAREN = new RegexElementType(Tokens.Rparen, ")");
	IElementType RE_EXPAND = new RegexElementType(Tokens.expand, "{expand}");

	IElementType RE_LSQUARE = new RegexElementType(Tokens.Lbrack, "[");
	IElementType RE_LSQUAREXOR = new RegexElementType(Tokens.LbrackXor, "[^");
	IElementType RE_MINUS = new RegexElementType(Tokens.Minus, "-");
	IElementType RE_RSQUARE = new RegexElementType(Tokens.Rbrack, "]");

	IElementType RE_SETDIFF = new RegexElementType(Tokens.op_minus, "{-}");
	IElementType RE_SETUNION = new RegexElementType(Tokens.op_union, "{+}");
	IElementType RE_INTERSECT = new RegexElementType(Tokens.op_intersect, "{&&}");
	IElementType RE_EOI = new RegexElementType(Tokens.kw_eoi, "{eoi}");

	IElementType RE_BAD = new RegexElementType(-1, "bad char");
}
