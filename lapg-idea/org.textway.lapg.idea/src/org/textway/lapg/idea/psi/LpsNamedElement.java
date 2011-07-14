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
package org.textway.lapg.idea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.parser.LapgElementTypes;

/**
 * Gryaznov Evgeny, 1/26/11
 */
public abstract class LpsNamedElement extends LpsElement implements PsiNamedElement {

	public LpsNamedElement(@NotNull ASTNode node) {
		super(node);
	}

	public LpsSymbol getNameSymbol() {
		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.SYMBOL));
		if (nodes != null && nodes.length == 1) {
			return (LpsSymbol) nodes[0].getPsi();
		}
		return null;
	}

	public String getName() {
		LpsSymbol nameSymbol = getNameSymbol();
		return nameSymbol != null ? nameSymbol.getText() : null;
	}

	public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
		LpsSymbol nameSymbol = getNameSymbol();
		if(nameSymbol == null) {
			throw new IncorrectOperationException();
		}
		nameSymbol.replace(LpsElementsFactory.createSymbol(getProject(), name));
		return this;
	}

}
