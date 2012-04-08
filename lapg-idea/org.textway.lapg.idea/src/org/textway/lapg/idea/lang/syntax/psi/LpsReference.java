/**
 * Copyright (c) 2010-2011 Evgeny Gryaznov
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
package org.textway.lapg.idea.lang.syntax.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Gryaznov Evgeny, 1/25/11
 */
public class LpsReference extends LpsElement implements PsiReference {

	public LpsReference(@NotNull ASTNode node) {
		super(node);
	}

	public String getReferenceText() {
		return getText();
	}

	public PsiElement resolve() {
		String referenceText = getReferenceText();
		if (referenceText == null) return null;
		PsiElement context = this.getContext();
		while (context != null) {
			if (context instanceof LpsGrammar) {
				LpsGrammar grammar = (LpsGrammar) context;
				return grammar.resolve(referenceText);
			}
			context = context.getContext();
		}
		return null;
	}

	@Override
	public PsiReference getReference() {
		return this;
	}

	public PsiElement getElement() {
		return this;
	}

	public TextRange getRangeInElement() {
		return new TextRange(0, getTextLength());
	}

	@NotNull
	public String getCanonicalText() {
		return getText();
	}

	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
		return replace(LpsElementsFactory.createReference(getProject(), newElementName));
	}

	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
		throw new IncorrectOperationException("Rebind cannot be performed for " + getClass());
	}

	public boolean isReferenceTo(PsiElement element) {
		return getElement().getManager().areElementsEquivalent(resolve(), element);
	}

	@NotNull
	public Object[] getVariants() {
		PsiElement context = this.getContext();
		while (context != null) {
			if (context instanceof LpsGrammar) {
				LpsGrammar grammar = (LpsGrammar) context;
				return grammar.getNamedElements();
			}
			context = context.getContext();
		}
		return new Object[0];
	}

	public boolean isSoft() {
		return false;
	}
}
