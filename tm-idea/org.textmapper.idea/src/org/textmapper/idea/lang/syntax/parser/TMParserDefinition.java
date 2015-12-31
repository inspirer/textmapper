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

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.TMElementType;
import org.textmapper.idea.lang.syntax.lexer.TMLexerAdapter;
import org.textmapper.idea.lang.syntax.lexer.TMTokenTypes;
import org.textmapper.idea.lang.syntax.psi.*;
import org.textmapper.tool.parser.TMParser.Nonterminals;

public class TMParserDefinition implements ParserDefinition {

	@NotNull
	public Lexer createLexer(Project project) {
		return new TMLexerAdapter();
	}

	public PsiParser createParser(Project project) {
		return new TMPsiParser();
	}

	public IFileElementType getFileNodeType() {
		return TextmapperElementTypes.FILE;
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return TMTokenTypes.whitespaces;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return TMTokenTypes.comments;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return TMTokenTypes.strings;
	}

	@NotNull
	public PsiElement createElement(ASTNode node) {
		IElementType type = node.getElementType();
		switch (((TMElementType) type).getSymbol()) {
			case Nonterminals.input:
				return new TmGrammar(node);
			case Nonterminals.header:
				return new TmHeader(node);
			case Nonterminals.import_:
				return new TmImport(node);
			case Nonterminals.option:
				return new TmOption(node);
			case Nonterminals.lexeme_attrs:
				return new TmLexemeAttrs(node);
			case Nonterminals.state_selector:
				return new TmLexerStateSelector(node);
			case Nonterminals.lexer_state:
				return new TmLexerState(node);
			case Nonterminals.rule0:
				return new TmRule(node);
			case Nonterminals.rhsPrefix:
				return new TmRhsPrefix(node);
			case Nonterminals.ruleAction:
				return new TmRuleAction(node);
			case Nonterminals.rhsSuffix:
				return new TmRhsSuffix(node);
			case Nonterminals.rhsAnnotated:
				return new TmRhsAnnotated(node);
			case Nonterminals.rhsUnordered:
				return new TmRhsUnordered(node);
			case Nonterminals.rhsPrimary:
				return new TmRhsPrimary(node);
			case Nonterminals.command:
				return new TmAction(node);
			case Nonterminals.type:
				return new TmType(node);
			case Nonterminals.annotation:
				return new TmAnnotation(node);
			case Nonterminals.expression:
				return new TmExpression(node);
			case Nonterminals.predicate:
				return new TmPredicate(node);
			case Nonterminals.predicate_expression:
				return new TmPredicateExpression(node);
			case Nonterminals.symref_args:
				return new TmSymbolArguments(node);
			case Nonterminals.symref:
			case Nonterminals.symref_noargs:
				return new TmSymbolReference(node);
			case Nonterminals.stateref:
				return new TmStateReference(node);
			case Nonterminals.identifier:
				return new TmIdentifier(node);
			case Nonterminals.qualified_id:
				return new TmQualifiedIdentifier(node);
			case Nonterminals.lexeme:
				return new TmLexeme(node);
			case Nonterminals.named_pattern:
				return new TmNamedPattern(node);
			case Nonterminals.nonterm:
				return new TmNonterm(node);
			case Nonterminals.template_param:
				return new TmTemplateParam(node);
			case Nonterminals.nonterm_params:
				return new TmNontermParams(node);
			case Nonterminals.nonterm_type:
				return new TmNontermType(node);
			case Nonterminals.directive:
				return new TmDirective(node);
			case Nonterminals.nonterm_param:
				return new TmNontermParam(node);
			case Nonterminals.lexer_directive:
				return new TmLexerDirective(node);
			case Nonterminals.param_ref:
				return new TmParameterReference(node);
			case Nonterminals.argument:
				return new TmTemplateArg(node);
			case Nonterminals.map_entry:
				return new TmMapEntry(node);
		}

		return new TmElement(node);
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new TMPsiFile(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}
}
