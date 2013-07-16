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
package org.textmapper.idea.lang.syntax.findUsages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.LapgLexerAdapter;
import org.textmapper.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textmapper.idea.lang.syntax.psi.*;

/**
 * evgeny, 8/11/12
 */
public class TMFindUsagesProvider implements FindUsagesProvider {
	@Override
	public WordsScanner getWordsScanner() {
		return new DefaultWordsScanner(new LapgLexerAdapter(),
				TokenSet.create(LapgTokenTypes.ID),
				LapgTokenTypes.comments,
				TokenSet.create(LapgTokenTypes.STRING));
	}

	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
		return psiElement instanceof TmNamedElement;
	}

	@Override
	public String getHelpId(@NotNull PsiElement psiElement) {
		return null;
	}

	@NotNull
	@Override
	public String getType(@NotNull PsiElement psiElement) {
		if (psiElement instanceof TmNonterm) {
			return "nonterm";
		}
		if (psiElement instanceof TmLexem) {
			return "lexem";
		}
		if (psiElement instanceof TmLexerState) {
			return "lexer state";
		}
		return "";
	}

	@NotNull
	@Override
	public String getDescriptiveName(@NotNull PsiElement element) {
		if (element instanceof PsiNamedElement) {
			final String name = ((PsiNamedElement) element).getName();
			return name == null ? "<unnamed>" : name;
		}
		return "";
	}

	@NotNull
	@Override
	public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
		return getDescriptiveName(element);
	}
}
