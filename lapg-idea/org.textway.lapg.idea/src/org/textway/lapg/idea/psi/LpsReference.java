/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.psi;

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
				return grammar.getSymbol(referenceText);
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
				return grammar.getSymbols();
			}
			context = context.getContext();
		}
		return new Object[0];
	}

	public boolean isSoft() {
		return false;
	}
}
