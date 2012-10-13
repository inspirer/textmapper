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

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.LapgFileType;

public class TmElement extends ASTWrapperPsiElement {

	IElementType type;

	public TmElement(@NotNull ASTNode node) {
		super(node);
		type = node.getElementType();
	}

	@NotNull
	@Override
	public Language getLanguage() {
		return LapgFileType.LAPG_LANGUAGE;
	}

	@NotNull
	@Override
	public SearchScope getUseScope() {
		return new LocalSearchScope(getContainingFile());
	}

	@Override
	public String toString() {
		return "lapg psi: " + type;
	}
}
