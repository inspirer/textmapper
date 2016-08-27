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
package org.textmapper.idea.lang.syntax.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.psi.TmGrammar;
import org.textmapper.idea.lang.syntax.psi.TmLexerState;
import org.textmapper.idea.lang.syntax.psi.TmLexerStateSelector;

import java.util.Map;

/**
 * Gryaznov Evgeny, 9/17/12
 */
public class RenameTMLexerStateProcessor extends RenamePsiElementProcessor {

	@Override
	public boolean canProcessElement(@NotNull PsiElement element) {
		return element instanceof TmLexerState;
	}

	@Override
	public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames, SearchScope scope) {
		String stateName = ((TmLexerState)element).getName();
		TmGrammar grammar = PsiTreeUtil.getTopmostParentOfType(element, TmGrammar.class);
		if (grammar != null && stateName != null) {
			for (TmLexerStateSelector selector : grammar.getStateSelectors()) {
				for (TmLexerState state : selector.getStates()) {
					if (stateName.equals(state.getName()) && state != element) {
						allRenames.put(state, newName);
					}
				}
			}
		}
	}
}
