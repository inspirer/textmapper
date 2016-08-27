/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
import org.textmapper.idea.lang.templates.lexer.LtplLexerAdapter;
import org.textmapper.idea.lang.templates.lexer.LtplTokenTypes;
import org.textmapper.idea.lang.templates.psi.TpsiBundle;
import org.textmapper.idea.lang.templates.psi.TpsiElement;
import org.textmapper.idea.lang.templates.psi.TpsiQuery;
import org.textmapper.idea.lang.templates.psi.TpsiTemplate;

/**
 * evgeny, 3/3/12
 */
public class LtplParserDefinition implements ParserDefinition {

	@NotNull
	public Lexer createLexer(Project project) {
		return new LtplLexerAdapter();
	}

	public PsiParser createParser(Project project) {
		return new LtplParser();
	}

	public IFileElementType getFileNodeType() {
		return LtplElementTypes.FILE;
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return LtplTokenTypes.whitespaces;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return LtplTokenTypes.comments;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return LtplTokenTypes.strings;
	}

	@NotNull
	public PsiElement createElement(ASTNode node) {
		IElementType type = node.getElementType();
		if (type == LtplElementTypes.BUNDLE) {
			return new TpsiBundle(node);
		} else if (type == LtplElementTypes.QUERY) {
			return new TpsiQuery(node);
		} else if (type == LtplElementTypes.TEMPLATE) {
			return new TpsiTemplate(node);
		}

		return new TpsiElement(node);
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LtplFile(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}
}
