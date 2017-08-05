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
package org.textmapper.idea.lang.syntax.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textmapper.idea.lang.syntax.TMFileType;
import org.textmapper.idea.lang.syntax.lexer.TMElementType;
import org.textmapper.tool.parser.TMParser.Nonterminals;

public interface TextmapperElementTypes {
	IFileElementType FILE = new IFileElementType(TMFileType.TM_LANGUAGE);
	IElementType GRAMMAR = new TMElementType(Nonterminals.input1, "grammar");

	IElementType HEADER = new TMElementType(Nonterminals.header, "header");
	IElementType IMPORT = new TMElementType(Nonterminals.import_, "import");
	IElementType OPTION = new TMElementType(Nonterminals.option, "option");

	IElementType LEXEM = new TMElementType(Nonterminals.lexeme, "lexem");
	IElementType NAMED_PATTERN = new TMElementType(Nonterminals.named_pattern, "pattern");
	IElementType LEXEM_ATTRS = new TMElementType(Nonterminals.lexeme_attrs, "lexeme attrs");
	IElementType LEXER_STATE = new TMElementType(Nonterminals.lexer_state, "lexer state");
	IElementType BRACKETS_DIRECTIVE = new TMElementType(Nonterminals.brackets_directive, "lexer directive");
	IElementType STATES_CLAUSE = new TMElementType(Nonterminals.states_clause, "states clause");
	IElementType START_CONDITIONS = new TMElementType(Nonterminals.start_conditions, "start conditions");
	IElementType START_CONDITIONS_SCOPE = new TMElementType(Nonterminals.start_conditions_scope, "start conditions scope");

	IElementType NONTERM = new TMElementType(Nonterminals.nonterm, "nonterminal");
	IElementType TEMPLATE_PARAM = new TMElementType(Nonterminals.template_param, "template parameter definition");
	IElementType NONTERM_PARAMS = new TMElementType(Nonterminals.nonterm_params, "nonterminal parameters");
	IElementType NONTERM_PARAM = new TMElementType(Nonterminals.nonterm_param, "nonterminal parameter definition");
	IElementType NONTERM_TYPE = new TMElementType(Nonterminals.nonterm_type, "nonterminal type");
	IElementType DIRECTIVE = new TMElementType(Nonterminals.directive, "directive");

	IElementType RULE = new TMElementType(Nonterminals.rule0, "rule");
	IElementType RHS_SUFFIX = new TMElementType(Nonterminals.rhsSuffix, "rhs suffix");
	IElementType REPORT_CLAUSE = new TMElementType(Nonterminals.reportClause, "report clause");
	IElementType RHS_PART = new TMElementType(Nonterminals.rhsAnnotated, "rhs part");
	IElementType RHS_PRIMARY = new TMElementType(Nonterminals.rhsPrimary, "rhs primary");
	IElementType RHS_STATE_MARKER = new TMElementType(Nonterminals.rhsStateMarker, "rhs state marker");

	IElementType ACTION = new TMElementType(Nonterminals.command, "action");
	IElementType RAW_TYPE = new TMElementType(Nonterminals.rawType, "raw type");
	IElementType LOOKAHEAD = new TMElementType(Nonterminals.rhsLookahead, "lookahead");
	IElementType LOOKAHEAD_PREDICATE = new TMElementType(Nonterminals.lookahead_predicate, "lookahead predicate");
	IElementType ANNOTATION = new TMElementType(Nonterminals.annotation, "annotation");
	IElementType EXPRESSION = new TMElementType(Nonterminals.expression, "expression");
	IElementType PREDICATE = new TMElementType(Nonterminals.predicate, "predicate");
	IElementType PREDICATE_EXPRESSION = new TMElementType(Nonterminals.predicate_expression, "predicate expression");
	IElementType SYMREF = new TMElementType(Nonterminals.symref, "symbol reference");
	IElementType SYMREF_ARGS = new TMElementType(Nonterminals.symref_args, "symbol arguments");
	IElementType SYMREF_NA = new TMElementType(Nonterminals.symref_noargs, "symbol reference");
	IElementType STATEREF = new TMElementType(Nonterminals.stateref, "state reference");
	IElementType IDENTIFIER = new TMElementType(Nonterminals.identifier, "symbol");
	IElementType QUALIFIED_ID = new TMElementType(Nonterminals.qualified_id, "qualified identifier");

	IElementType PARAMREF = new TMElementType(Nonterminals.param_ref, "parameter reference");
	IElementType TEMPLATE_ARG = new TMElementType(Nonterminals.argument, "template argument");

	IElementType[] allElements = {
			HEADER, IMPORT, OPTION,
			LEXEM, NAMED_PATTERN, LEXEM_ATTRS, LEXER_STATE, BRACKETS_DIRECTIVE, STATES_CLAUSE,
			START_CONDITIONS, START_CONDITIONS_SCOPE,
			NONTERM, TEMPLATE_PARAM, NONTERM_PARAMS, NONTERM_PARAM, NONTERM_TYPE, DIRECTIVE,
			RULE, RHS_SUFFIX, REPORT_CLAUSE, RHS_PART, RHS_PRIMARY, RHS_STATE_MARKER,
			ACTION, RAW_TYPE, LOOKAHEAD, LOOKAHEAD_PREDICATE, ANNOTATION, EXPRESSION, PREDICATE, PREDICATE_EXPRESSION,
			SYMREF, SYMREF_ARGS, SYMREF_NA, STATEREF, IDENTIFIER, QUALIFIED_ID,
			PARAMREF, TEMPLATE_ARG
	};
}
