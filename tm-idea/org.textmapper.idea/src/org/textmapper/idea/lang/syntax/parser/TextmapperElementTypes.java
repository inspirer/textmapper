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
import org.textmapper.idea.lang.syntax.TMFileType;
import org.textmapper.idea.lang.syntax.lexer.TMElementType;
import org.textmapper.tool.parser.TMParser.Tokens;

public interface TextmapperElementTypes {
	final IFileElementType FILE = new IFileElementType(TMFileType.TM_LANGUAGE);
	public static final IElementType GRAMMAR = new TMElementType(Tokens.input, "grammar");

	public static final IElementType HEADER = new TMElementType(Tokens.header, "header");
	public static final IElementType IMPORT = new TMElementType(Tokens.import_, "import");
	public static final IElementType OPTION = new TMElementType(Tokens.option, "option");

	public static final IElementType LEXEM = new TMElementType(Tokens.lexeme, "lexem");
	public static final IElementType NAMED_PATTERN = new TMElementType(Tokens.named_pattern, "pattern");
	public static final IElementType STATE_SELECTOR = new TMElementType(Tokens.state_selector, "lexer state selector");
	public static final IElementType LEXEM_ATTRS = new TMElementType(Tokens.lexem_attrs, "lexem attrs");
	public static final IElementType LEXER_STATE = new TMElementType(Tokens.lexer_state, "lexer state");

	public static final IElementType NONTERM = new TMElementType(Tokens.nonterm, "nonterminal");
	public static final IElementType NONTERM_TYPE = new TMElementType(Tokens.nonterm_type, "nonterminal type");
	public static final IElementType DIRECTIVE = new TMElementType(Tokens.directive, "directive");

	public static final IElementType RULE = new TMElementType(Tokens.rule0, "rule");
	public static final IElementType RHS_PREFIX = new TMElementType(Tokens.rhsPrefix, "rhs prefix");
	public static final IElementType RHS_SUFFIX = new TMElementType(Tokens.rhsSuffix, "rhs suffix");
	public static final IElementType RHS_PART = new TMElementType(Tokens.rhsAnnotated, "rhs part");
	public static final IElementType RHS_UNORDERED = new TMElementType(Tokens.rhsUnordered, "rhs unordered");
	public static final IElementType RHS_PRIMARY = new TMElementType(Tokens.rhsPrimary, "rhs primary");
	public static final IElementType RHS_BRACKETS_PAIR = new TMElementType(Tokens.rhsBracketsPair, "brackets pair");

	public static final IElementType ACTION = new TMElementType(Tokens.command, "action");
	public static final IElementType TYPE = new TMElementType(Tokens.type, "type");
	public static final IElementType ANNOTATION = new TMElementType(Tokens.annotation, "annotation");
	public static final IElementType EXPRESSION = new TMElementType(Tokens.expression, "expression");
	public static final IElementType SYMREF = new TMElementType(Tokens.symref, "symbol reference");
	public static final IElementType STATEREF = new TMElementType(Tokens.stateref, "state reference");
	public static final IElementType IDENTIFIER = new TMElementType(Tokens.identifier, "symbol");
	public static final IElementType QUALIFIED_ID = new TMElementType(Tokens.qualified_id, "qualified identifier");

	public static final IElementType[] allElements = {
			HEADER, IMPORT, OPTION,
			LEXEM, STATE_SELECTOR, NAMED_PATTERN, LEXEM_ATTRS, LEXER_STATE,
			NONTERM, NONTERM_TYPE, DIRECTIVE,
			RULE, RHS_PREFIX, RHS_SUFFIX, RHS_PART, RHS_UNORDERED, RHS_PRIMARY, RHS_BRACKETS_PAIR,
			ACTION, TYPE, ANNOTATION, EXPRESSION, SYMREF, STATEREF, IDENTIFIER, QUALIFIED_ID
	};
}
