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
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.parser.LapgElementTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 1/26/11
 */
public class LpsGrammar extends LpsElement {

	public LpsGrammar(@NotNull ASTNode node) {
		super(node);
	}

	public LpsSymbol[] getSymbols() {
		List<LpsSymbol> result = new ArrayList<LpsSymbol>();
		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.SYMBOL));
		for (int i = 0; i < nodes.length; i++) {
		  result.add((LpsSymbol)nodes[i].getPsi());
		}
		return result.toArray(new LpsSymbol[result.size()]);
	}

	public LpsSymbol getSymbol(String name) {
		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.SYMBOL));
		for (ASTNode node : nodes) {
			LpsSymbol sym = (LpsSymbol) node.getPsi();
			if (sym != null && name.equals(sym.getName())) {
				return sym;
			}
		}
		return null;
	}
}
