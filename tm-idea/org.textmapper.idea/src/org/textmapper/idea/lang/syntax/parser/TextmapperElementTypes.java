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
package org.textmapper.idea.lang.syntax.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textmapper.idea.lang.syntax.LapgFileType;
import org.textmapper.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.tool.parser.TMParser.Tokens;

public interface TextmapperElementTypes {
	final IFileElementType FILE = new IFileElementType(LapgFileType.LAPG_LANGUAGE);

	public static final IElementType GRAMMAR = new LapgElementType(Tokens.input, "grammar");

	public static final IElementType OPTION = new LapgElementType(Tokens.option, "option");

	public static final IElementType LEXEM = new LapgElementType(Tokens.lexeme, "lexem");
	public static final IElementType NAMED_PATTERN = new LapgElementType(Tokens.named_pattern, "pattern");
	public static final IElementType STATE_SELECTOR = new LapgElementType(Tokens.state_selector, "lexer state selector");
	public static final IElementType LEXEM_ATTRS = new LapgElementType(Tokens.lexem_attrs, "lexem attrs");
	public static final IElementType LEXER_STATE = new LapgElementType(Tokens.lexer_state, "lexer state");

	public static final IElementType NONTERM = new LapgElementType(Tokens.nonterm, "non-terminal");
	public static final IElementType DIRECTIVE = new LapgElementType(Tokens.directive, "directive");

	public static final IElementType RULE = new LapgElementType(Tokens.rule0, "rule");
	public static final IElementType RHS_PREFIX = new LapgElementType(Tokens.rhsPrefix, "rhs prefix");
	public static final IElementType RHS_SUFFIX = new LapgElementType(Tokens.rhsSuffix, "rhs suffix");
	public static final IElementType RHS_PART = new LapgElementType(Tokens.rhsAnnotated, "rhs part");
	public static final IElementType RHS_UNORDERED = new LapgElementType(Tokens.rhsUnordered, "rhs unordered");
	public static final IElementType RHS_PRIMARY = new LapgElementType(Tokens.rhsPrimary, "rhs primary");
	public static final IElementType NEGATIVE_LA = new LapgElementType(Tokens.negative_la, "negative_la");

	public static final IElementType ACTION = new LapgElementType(Tokens.command, "action");
	public static final IElementType TYPE = new LapgElementType(Tokens.type, "type");
	public static final IElementType ANNOTATION = new LapgElementType(Tokens.annotation, "annotation");
	public static final IElementType EXPRESSION = new LapgElementType(Tokens.expression, "expression");
	public static final IElementType SYMREF = new LapgElementType(Tokens.symref, "symbol reference");
	public static final IElementType STATEREF = new LapgElementType(Tokens.stateref, "state reference");
	public static final IElementType IDENTIFIER = new LapgElementType(Tokens.identifier, "symbol");
	public static final IElementType QUALIFIED_ID = new LapgElementType(Tokens.qualified_id, "qualified identifier");

	public static final IElementType[] allElements = {
			OPTION,
			LEXEM, STATE_SELECTOR, NAMED_PATTERN, LEXEM_ATTRS, LEXER_STATE,
			NONTERM, DIRECTIVE,
			RULE, RHS_PREFIX, RHS_SUFFIX, RHS_PART, RHS_UNORDERED, RHS_PRIMARY, NEGATIVE_LA,
			ACTION, TYPE, ANNOTATION, EXPRESSION, SYMREF, STATEREF, IDENTIFIER, QUALIFIED_ID
	};
}
