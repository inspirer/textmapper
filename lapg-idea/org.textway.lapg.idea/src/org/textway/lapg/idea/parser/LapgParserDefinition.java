/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.parser;

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
import org.textway.lapg.idea.lexer.LapgLexerAdapter;
import org.textway.lapg.idea.lexer.LapgTokenTypes;
import org.textway.lapg.idea.psi.*;

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
		if (type == LapgElementTypes.REFERENCE) {
			return new LpsReference(node);
		} else if (type == LapgElementTypes.SYMBOL) {
			return new LpsSymbol(node);
		} else if (type == LapgElementTypes.OPTION) {
			return new LpsOption(node);
		} else if (type == LapgElementTypes.GRAMMAR) {
			return new LpsGrammar(node);
		} else if (type == LapgElementTypes.RULE) {
			return new LpsRule(node);
		} else if (type == LapgElementTypes.NONTERM) {
			return new LpsNonTerm(node);
		} else if (type == LapgElementTypes.LEXEM) {
			return new LpsLexem(node);
		} else if (type == LapgElementTypes.ACTION) {
			return new LpsAction(node);
		}

		return new LpsElement(node);
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LapgFile(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}
}
