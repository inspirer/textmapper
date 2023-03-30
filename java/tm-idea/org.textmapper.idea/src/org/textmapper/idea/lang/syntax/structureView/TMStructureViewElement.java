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
package org.textmapper.idea.lang.syntax.structureView;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;
import org.textmapper.idea.lang.syntax.psi.TmGrammar;
import org.textmapper.idea.lang.syntax.psi.TmLexeme;
import org.textmapper.idea.lang.syntax.psi.TmNamedElement;
import org.textmapper.idea.lang.syntax.psi.TmNonterm;

import javax.swing.*;
import java.util.List;

/**
 * evgeny, 8/11/12
 */
public class TMStructureViewElement implements StructureViewTreeElement {
	private final NavigatablePsiElement myElement;
	private String elementText;

	public TMStructureViewElement(NavigatablePsiElement element) {
		myElement = element;
	}


	@Override
	public void navigate(boolean requestFocus) {
		myElement.navigate(requestFocus);
	}

	@Override
	public boolean canNavigate() {
		return myElement.canNavigate();
	}

	@Override
	public boolean canNavigateToSource() {
		return myElement.canNavigateToSource();
	}

	@Override
	public Object getValue() {
		return myElement;
	}

	@Override
	public ItemPresentation getPresentation() {
		return new ItemPresentation() {
			@Override
			public String getPresentableText() {
				if (elementText == null) {
					elementText = getElementText();
				}

				return elementText;
			}

			@Override
			public String getLocationString() {
				return null;
			}

			@Override
			public Icon getIcon(boolean open) {
				return getElementIcon();
			}
		};

	}

	private String getElementText() {
		if (myElement instanceof TMPsiFile) {
			return "file";
		} else if (myElement instanceof TmLexeme) {
			return myElement.getName();
		} else if (myElement instanceof TmNonterm) {
			return myElement.getName();
		}
		return "unknown";
	}

	private Icon getElementIcon() {
		if (myElement instanceof TMPsiFile) {
			return TMIcons.TM_ICON;
		}
		return null;
	}

	@NotNull
	@Override
	public TreeElement[] getChildren() {
		if (myElement instanceof TMPsiFile) {
			final TMPsiFile tmFile = (TMPsiFile) myElement;
			TmGrammar grammar = tmFile.getGrammar();
			if (grammar != null) {
				return wrapDeclarations(grammar.getNonterms());
			}
		}
		return new TreeElement[0];  //To change body of implemented methods use File | Settings | File Templates.
	}

	private TreeElement[] wrapDeclarations(List<? extends TmNamedElement> declarations) {
		TreeElement[] result = new TreeElement[declarations.size()];
		for (int i = 0; i < declarations.size(); i++) {
			result[i] = new TMStructureViewElement(declarations.get(i));
		}
		return result;
	}
}
