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
package org.textmapper.lapg.idea.lang.syntax.psi;

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

	public TmNamedElement resolve(String name) {
		if (name.endsWith("opt") && name.length() > 3) {
			name = name.substring(0, name.length() - 3);
		}

		for (TmNamedElement named : getNamedElements()) {
			if (name.equals(named.getName())) {
				return named;
			}
		}
		return null;
	}

	public List<TmOption> getOptions() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmOption.class);
	}

	public List<TmLexem> getLexems() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmLexem.class);
	}

	public List<TmNonTerm> getNonTerms() {
		return PsiTreeUtil.getChildrenOfTypeAsList(this, TmNonTerm.class);
	}
}
