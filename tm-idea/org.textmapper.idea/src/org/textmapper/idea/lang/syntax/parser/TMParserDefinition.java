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
import org.textmapper.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.idea.lang.syntax.lexer.LapgLexerAdapter;
import org.textmapper.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textmapper.idea.lang.syntax.psi.*;
import org.textmapper.tool.parser.TMParser.Tokens;

public class TMParserDefinition implements ParserDefinition {

	@NotNull
	public Lexer createLexer(Project project) {
		return new LapgLexerAdapter();
	}

	public PsiParser createParser(Project project) {
		return new TMPsiParser();
	}

	public IFileElementType getFileNodeType() {
		return TextmapperElementTypes.FILE;
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return LapgTokenTypes.whitespaces;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return LapgTokenTypes.comments;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return LapgTokenTypes.strings;
	}

	@NotNull
	public PsiElement createElement(ASTNode node) {
		IElementType type = node.getElementType();
		switch (((LapgElementType) type).getSymbol()) {
			case Tokens.input:
				return new TmGrammar(node);
			case Tokens.option:
				return new TmOption(node);
			case Tokens.lexem_attrs:
				return new TmLexemAttrs(node);
			case Tokens.state_selector:
				return new TmLexerStateSelector(node);
			case Tokens.lexer_state:
				return new TmLexerState(node);
			case Tokens.rule0:
				return new TmRule(node);
			case Tokens.rules:
				return new TmRuleGroup(node);
			case Tokens.rhsPrefix:
				return new TmRhsPrefix(node);
			case Tokens.rhsSuffix:
				return new TmRhsSuffix(node);
			case Tokens.rhsAnnotated:
				return new TmRhsAnnotated(node);
			case Tokens.rhsUnordered:
				return new TmRhsUnordered(node);
			case Tokens.rhsPrimary:
				return new TmRhsPrimary(node);
			case Tokens.negative_la:
				return new TmNegativeLA(node);
			case Tokens.command:
				return new TmAction(node);
			case Tokens.type:
				return new TmType(node);
			case Tokens.annotation:
				return new TmAnnotation(node);
			case Tokens.expression:
				return new TmExpression(node);
			case Tokens.symref:
				return new TmSymbolReference(node);
			case Tokens.stateref:
				return new TmStateReference(node);
			case Tokens.identifier:
				return new TmIdentifier(node);
			case Tokens.qualified_id:
				return new TmQualifiedIdentifier(node);
			case Tokens.lexeme:
				return new TmLexem(node);
			case Tokens.named_pattern:
				return new TmNamedPattern(node);
			case Tokens.nonterm:
				return new TmNonterm(node);
			case Tokens.nonterm_type:
				return new TmNontermType(node);
			case Tokens.directive:
				return new TmDirective(node);
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
