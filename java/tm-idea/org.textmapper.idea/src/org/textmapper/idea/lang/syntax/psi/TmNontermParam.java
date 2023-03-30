/**
 * Copyright 2010-2017 Evgeny Gryaznov
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
package org.textmapper.idea.lang.syntax.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class TmNontermParam extends TmNamedElement {
	public TmNontermParam(@NotNull ASTNode node) {
		super(node);
	}

	boolean isInline() {
		return !(getFirstChild() instanceof TmParameterReference);
	}

	@Override
	public TmIdentifier getNameIdentifier() {
		TmIdentifier[] all = PsiTreeUtil.getChildrenOfType(this, TmIdentifier.class);
		if (all != null && all.length >= 2) {
			return all[1];
		}
		return null;
	}
}
