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
package org.textmapper.lapg.idea.lang.syntax.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textmapper.lapg.idea.lang.syntax.LapgFileType;
import org.textmapper.lapg.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.lapg.parser.LapgParser.Tokens;

public interface LapgElementTypes {
	final IFileElementType FILE = new IFileElementType(LapgFileType.LAPG_LANGUAGE);

	public static final IElementType GRAMMAR = new LapgElementType(Tokens.input, "grammar");

	public static final IElementType OPTION = new LapgElementType(Tokens.option, "option");

	public static final IElementType LEXEM = new LapgElementType(-1, "lexem");
	public static final IElementType STATE_SELECTOR = new LapgElementType(-1, "lexer state");
	public static final IElementType NAMED_PATTERN = new LapgElementType(-1, "pattern");
	public static final IElementType LEXEM_ATTRS = new LapgElementType(Tokens.lexem_attrs, "lexem attrs");

	public static final IElementType NONTERM = new LapgElementType(-1, "non-terminal");
	public static final IElementType DIRECTIVE = new LapgElementType(-1, "directive");

	public static final IElementType RULE = new LapgElementType(Tokens.rule0, "rule");
	public static final IElementType RULES = new LapgElementType(Tokens.rules, "rules");
	public static final IElementType RULEPREFIX = new LapgElementType(Tokens.ruleprefix, "ruleprefix");
	public static final IElementType RULEATTRS = new LapgElementType(Tokens.rule_attrs, "ruleattrs");
	public static final IElementType RULEPART = new LapgElementType(Tokens.rulepart, "rulepart");
	public static final IElementType RULESYMREF = new LapgElementType(Tokens.rulesymref, "rulesymref");
	public static final IElementType NEGATIVE_LA = new LapgElementType(Tokens.negative_la, "negative_la");

	public static final IElementType ACTION = new LapgElementType(Tokens.command, "action");
	public static final IElementType TYPE = new LapgElementType(Tokens.type, "type");
	public static final IElementType ANNOTATION = new LapgElementType(Tokens.annotation, "annotation");
	public static final IElementType EXPRESSION = new LapgElementType(Tokens.expression, "expression");
	public static final IElementType REFERENCE = new LapgElementType(Tokens.reference, "reference");
	public static final IElementType SYMBOL = new LapgElementType(Tokens.symbol, "symbol");
	public static final IElementType QUALIFIED_ID = new LapgElementType(Tokens.qualified_id, "qualified identifier");

	public static final IElementType[] allElements = {
			OPTION,
			LEXEM, STATE_SELECTOR, NAMED_PATTERN, LEXEM_ATTRS,
			NONTERM, DIRECTIVE,
			RULE, RULES, RULEPREFIX, RULEATTRS, RULEPART, RULESYMREF, NEGATIVE_LA,
			ACTION, TYPE, ANNOTATION, EXPRESSION, REFERENCE, SYMBOL, QUALIFIED_ID
	};
}
