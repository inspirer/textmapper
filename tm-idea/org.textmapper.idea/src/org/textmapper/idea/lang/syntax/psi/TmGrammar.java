/**
 * Copyright 2010-2017 Evgeny Gryaznov
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Gryaznov Evgeny, 1/26/11
 */
public class TmGrammar extends TmElement {

	public TmGrammar(@NotNull ASTNode node) {
		super(node);
	}

	public TmNamedElement[] getNamedElements() {
		return PsiTreeUtil.getChildrenOfType(this, TmNamedElement.class);
	}

	public TmHeader getHeader() {
		return PsiTreeUtil.getChildOfType(this, TmHeader.class);
	}

	public List<TmImport> getImports() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmImport.class);
	}

	public List<TmOption> getOptions() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmOption.class);
	}

	public List<TmLexeme> getLexemes() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmLexeme.class);
	}

	public List<TmNonterm> getNonterms() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmNonterm.class);
	}

	public List<TmStatesClause> getStateDeclarations() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmStatesClause.class);
	}

	public List<TmLexerStateSelector> getStateSelectors() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmLexerStateSelector.class);
	}

	public TmNamedElement resolve(String name) {
		if (name.endsWith("opt") && name.length() > 3) {
			name = name.substring(0, name.length() - 3);
		}

		TmNamedElement[] namedElements = getNamedElements();
		if (namedElements == null) {
			return null;
		}

		for (TmNamedElement named : namedElements) {
			if (name.equals(named.getName())) {
				return named;
			}
		}
		return null;
	}

	public TmNamedElement resolveState(String name) {
		for (TmStatesClause clause : getStateDeclarations()) {
			for (TmLexerState state : clause.getStates()) {
				if (name.equals(state.getName())) {
					return state;
				}
			}
		}
		return null;
	}
}
