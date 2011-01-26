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
