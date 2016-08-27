/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
package org.textmapper.idea.lang.templates.parser;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.templates.LtplFileType;
import org.textmapper.idea.lang.templates.psi.TpsiBundle;

/**
 * evgeny, 3/3/12
 */
public class LtplFile extends PsiFileImpl {
	protected LtplFile(FileViewProvider viewProvider) {
		super(LtplElementTypes.FILE, LtplElementTypes.FILE, viewProvider);
	}

	@NotNull
	public FileType getFileType() {
		return LtplFileType.LTPL_FILE_TYPE;
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		visitor.visitFile(this);
	}

	public TpsiBundle getBundle() {
		ASTNode[] children = getNode().getChildren(TokenSet.create(LtplElementTypes.BUNDLE));
		if (children.length == 1) {
			return (TpsiBundle) children[0].getPsi();
		}
		return null;
	}

	public String toString() {
		return "LapgTemplatesFile(bundle):" + getName();
	}
}
