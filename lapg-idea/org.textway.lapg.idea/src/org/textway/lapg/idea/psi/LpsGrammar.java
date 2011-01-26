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

	public LpsNamedElement[] getNamedElements() {
		List<LpsNamedElement> result = new ArrayList<LpsNamedElement>();
		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.NONTERM, LapgElementTypes.LEXEM));
		for (ASTNode node : nodes) {
			LpsNamedElement named = (LpsNamedElement) node.getPsi();
			if (named == null) {
				continue;
			}
			result.add(named);
		}
		return result.toArray(new LpsNamedElement[result.size()]);
	}

	public LpsNamedElement resolve(String name) {
		if(name.endsWith("opt") && name.length() > 3) {
			name = name.substring(0, name.length() - 3);
		}

		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.NONTERM, LapgElementTypes.LEXEM));
		for (ASTNode node : nodes) {
			LpsNamedElement named = (LpsNamedElement) node.getPsi();
			if (named == null) {
				continue;
			}
			if (name.equals(named.getName())) {
				return named;
			}
		}
		return null;
	}

	public LpsNonTerm[] getNonTerms() {
		final ASTNode[] nodes = getNode().getChildren(TokenSet.create(LapgElementTypes.NONTERM));
		List<LpsNonTerm> result = new ArrayList<LpsNonTerm>(nodes.length);
		for (ASTNode node : nodes) {
			result.add((LpsNonTerm) node.getPsi());
		}
		return result.toArray(new LpsNonTerm[result.size()]);
	}
}
