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
package org.textmapper.idea.lang.syntax.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gryaznov Evgeny, 9/12/12
 */
public class TmStateReference extends TmElement implements PsiPolyVariantReference {

	public TmStateReference(@NotNull ASTNode node) {
		super(node);
	}

	public String getReferenceText() {
		return getText();
	}

	@Nullable
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
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
		if (grammar != null) {
			List<TmLexerState> states = new ArrayList<TmLexerState>();
			Set<String> seen = new HashSet<String>();
			for (TmLexerStateSelector selector : grammar.getStateSelectors()) {
				for (TmLexerState tmLexerState : selector.getStates()) {
					if (seen.add(tmLexerState.getName())) {
						states.add(tmLexerState);
					}
				}
			}
			return states.toArray();
		}
		return new Object[0];
	}

	public boolean isSoft() {
		return false;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode) {
		String referenceText = getReferenceText();
		TmGrammar grammar = PsiTreeUtil.getTopmostParentOfType(this, TmGrammar.class);
		if (grammar != null && referenceText != null) {
			List<ResolveResult> states = new ArrayList<ResolveResult>();
			for (TmLexerStateSelector selector : grammar.getStateSelectors()) {
				for (TmLexerState state : selector.getStates()) {
					if (referenceText.equals(state.getName())) {
						states.add(new PsiElementResolveResult(state));
					}
				}
			}
			return states.toArray(new ResolveResult[states.size()]);
		}
		return new ResolveResult[0];
	}
}
