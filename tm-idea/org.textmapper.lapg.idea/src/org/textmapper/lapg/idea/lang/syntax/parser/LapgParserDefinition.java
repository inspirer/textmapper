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
import org.textmapper.lapg.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.lapg.idea.lang.syntax.lexer.LapgLexerAdapter;
import org.textmapper.lapg.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textmapper.lapg.idea.lang.syntax.psi.*;
import org.textmapper.lapg.parser.LapgParser.Tokens;

public class LapgParserDefinition implements ParserDefinition {

	@NotNull
	public Lexer createLexer(Project project) {
		return new LapgLexerAdapter();
	}

	public PsiParser createParser(Project project) {
		return new LapgParser();
	}

	public IFileElementType getFileNodeType() {
		return LapgElementTypes.FILE;
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
		if (type == LapgElementTypes.LEXEM) {
			return new TmLexem(node);
		} else if (type == LapgElementTypes.STATE_SELECTOR) {
			return new TmLexerStateSelector(node);
		} else if (type == LapgElementTypes.NAMED_PATTERN) {
			return new TmNamedPattern(node);
		}

		if (type == LapgElementTypes.NONTERM) {
			return new TmNonTerm(node);
		} else if(type == LapgElementTypes.DIRECTIVE) {
			return new TmDirective(node);
		}

		switch (((LapgElementType) type).getSymbol()) {
			case Tokens.input:
				return new TmGrammar(node);
			case Tokens.option:
				return new TmOption(node);
			case Tokens.lexem_attrs:
				return new TmLexemAttrs(node);
			case Tokens.rule0:
				return new TmRule(node);
			case Tokens.rules:
				return new TmRuleGroup(node);
			case Tokens.ruleprefix:
				return new TmRulePrefix(node);
			case Tokens.rule_attrs:
				return new TmRuleAttrs(node);
			case Tokens.rulepart:
				return new TmRulePart(node);
			case Tokens.rulesymref:
				return new TmRuleSymRef(node);
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
			case Tokens.reference:
				return new TmReference(node);
			case Tokens.symbol:
				return new TmSymbol(node);
			case Tokens.qualified_id:
				return new TmQualifiedIdentifier(node);
		}

		return new TmElement(node);
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LapgFile(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}
}
