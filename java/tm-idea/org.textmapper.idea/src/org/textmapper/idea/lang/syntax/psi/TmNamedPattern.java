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
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.TMTokenTypes;

/**
 * evgeny, 8/14/12
 */
public class TmNamedPattern extends TmElement implements PsiNamedElement {

	public TmNamedPattern(@NotNull ASTNode node) {
		super(node);
	}

	public ASTNode getIdentifier() {
		return getNode().findChildByType(TMTokenTypes.ID);
	}

	public String getName() {
		ASTNode nameSymbol = getIdentifier();
		return nameSymbol != null ? nameSymbol.getText() : null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
		throw new IncorrectOperationException();
	}
}
