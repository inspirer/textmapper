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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TmStateReference extends TmElement implements PsiReference {

	public TmStateReference(@NotNull ASTNode node) {
		super(node);
	}

	public String getReferenceText() {
		return getText();
	}

	@Nullable
	public PsiElement resolve() {
		String referenceText = getReferenceText();
		if (referenceText == null) return null;
		TmGrammar grammar = PsiTreeUtil.getTopmostParentOfType(this, TmGrammar.class);
		if (grammar == null) return null;
		return grammar.resolveState(referenceText);
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
		return replace(TmElementsFactory.createStateReference(getProject(), newElementName));
	}

	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
		throw new IncorrectOperationException("Rebind cannot be performed for " + getClass());
	}

	public boolean isReferenceTo(PsiElement element) {
		if (element.getContainingFile() != this.getContainingFile()) return false;
		String referenceText = getReferenceText();
		if (referenceText == null) return false;
		return element instanceof TmLexerState && referenceText.equals(((TmLexerState) element).getName());
	}

	@NotNull
	public Object[] getVariants() {
		TmGrammar grammar = PsiTreeUtil.getTopmostParentOfType(this, TmGrammar.class);
		if (grammar == null) return new Object[0];
		return grammar.getStates();
	}

	public boolean isSoft() {
		return false;
	}
}
