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
package org.textway.lapg.idea.lang.regex.lexer;

import com.intellij.psi.tree.IElementType;
import org.textway.lapg.regex.RegexDefLexer.Lexems;

/**
 * evgeny, 3/4/12
 */
public interface RegexTokenTypes {

	IElementType RE_DELIMITERS = new RegexElementType(-1, "/");

	IElementType RE_CHAR = new RegexElementType(Lexems._char, "char");
	IElementType RE_ESCAPED = new RegexElementType(Lexems.escaped, "\\escaped");
	IElementType RE_CHARCLASS = new RegexElementType(Lexems.charclass, "charclass");

	IElementType RE_DOT = new RegexElementType(Lexems.DOT, ".");
	IElementType RE_MULT = new RegexElementType(Lexems.MULT, "*");
	IElementType RE_PLUS = new RegexElementType(Lexems.PLUS, "+");
	IElementType RE_QUESTIONMARK = new RegexElementType(Lexems.QUESTIONMARK, "?");
	IElementType RE_QUANTFIER = new RegexElementType(Lexems.quantifier, "{n,m}");

	IElementType RE_LPAREN = new RegexElementType(Lexems.LPAREN, "(");
	IElementType RE_LPARENQMARK = new RegexElementType(Lexems.LPARENQUESTIONMARK, "(?");
	IElementType RE_OR = new RegexElementType(Lexems.OR, "|");
	IElementType RE_RPAREN = new RegexElementType(Lexems.RPAREN, ")");
	IElementType RE_EXPAND = new RegexElementType(Lexems.expand, "{expand}");

	IElementType RE_LSQUARE = new RegexElementType(Lexems.LSQUARE, "[");
	IElementType RE_LSQUAREXOR = new RegexElementType(Lexems.LSQUAREXOR, "[^");
	IElementType RE_MINUS = new RegexElementType(Lexems.MINUS, "-");
	IElementType RE_RSQUARE = new RegexElementType(Lexems.RSQUARE, "]");

	IElementType RE_SETDIFF = new RegexElementType(Lexems.op_minus, "{-}");
	IElementType RE_SETUNION = new RegexElementType(Lexems.op_union, "{+}");
	IElementType RE_INTERSECT = new RegexElementType(Lexems.op_intersect, "{&&}");
	IElementType RE_EOI = new RegexElementType(Lexems.kw_eoi, "{eoi}");

	IElementType RE_BAD = new RegexElementType(-1, "bad char");
}
