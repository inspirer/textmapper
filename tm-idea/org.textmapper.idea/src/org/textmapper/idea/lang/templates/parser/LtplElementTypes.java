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
package org.textmapper.idea.lang.templates.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textmapper.idea.lang.templates.LtplFileType;
import org.textmapper.idea.lang.templates.lexer.LtplElementType;
import org.textmapper.templates.ast.TemplatesParser.Nonterminals;

/**
 * evgeny, 3/3/12
 */
public interface LtplElementTypes {
	IFileElementType FILE = new IFileElementType(LtplFileType.LTPL_LANGUAGE);

	IElementType BUNDLE = new LtplElementType(Nonterminals.input, "bundle");
	IElementType TEMPLATE_BODY = new LtplElementType(Nonterminals.body, "body");

	IElementType TEMPLATE = new LtplElementType(Nonterminals.template_def, "template");
	IElementType QUERY = new LtplElementType(Nonterminals.query_def, "query");
	IElementType INSTRUCTION = new LtplElementType(Nonterminals.instruction, "instruction");

	IElementType[] allElements = {
			TEMPLATE, QUERY, INSTRUCTION
	};

	IElementType EXPRESSION = new LtplElementType(Nonterminals.expression, "expression");

	int[] allExpressions = {
			Nonterminals.primary_expression,
			Nonterminals.unary_expression,
			Nonterminals.binary_op,
			Nonterminals.instanceof_expression,
			Nonterminals.equality_expression,
			Nonterminals.conditional_op,
			Nonterminals.assignment_expression,
			Nonterminals.expression,
	};
}
