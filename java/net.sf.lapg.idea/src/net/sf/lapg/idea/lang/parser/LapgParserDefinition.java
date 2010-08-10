/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.lapg.idea.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilBase;
import net.sf.lapg.idea.lang.LapgLexerAdapter;
import org.jetbrains.annotations.NotNull;

public class LapgParserDefinition implements ParserDefinition {

	@NotNull
	public Lexer createLexer(Project project) {
		return new LapgLexerAdapter();
	}

	public PsiParser createParser(Project project) {
		return PsiUtil.NULL_PARSER;
	}

	public IFileElementType getFileNodeType() {
		return LapgElementTypes.FILE;
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return TokenSet.EMPTY;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return TokenSet.EMPTY;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@NotNull
	public PsiElement createElement(ASTNode node) {
		return PsiUtilBase.NULL_PSI_ELEMENT;
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LapgFile(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}
}
