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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class TmNamedElement extends TmElement implements PsiNameIdentifierOwner {

	public TmNamedElement(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement() {
		TmIdentifier nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? this : nameIdentifier;
	}

	@Override
	public TmIdentifier getNameIdentifier() {
		return PsiTreeUtil.getChildOfType(this, TmIdentifier.class);
	}

	public String getName() {
		TmIdentifier nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getText() : null;
	}

	public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
		TmIdentifier nameIdentifier = getNameIdentifier();
		if (nameIdentifier == null) {
			throw new IncorrectOperationException();
		}
		nameIdentifier.replace(TmElementsFactory.createIdentifier(getProject(), name));
		return this;
	}
}
