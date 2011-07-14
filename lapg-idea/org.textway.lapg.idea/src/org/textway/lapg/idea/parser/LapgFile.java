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
package org.textway.lapg.idea.parser;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.TokenSet;
import org.textway.lapg.idea.file.LapgFileType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.psi.LpsGrammar;

public class LapgFile extends PsiFileImpl {
	protected LapgFile(FileViewProvider viewProvider) {
		super(LapgElementTypes.FILE, LapgElementTypes.FILE, viewProvider);
	}

	@NotNull
	public FileType getFileType() {
		return LapgFileType.LAPG_FILE_TYPE;
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		visitor.visitFile(this);
	}

	public LpsGrammar getGrammar() {
		ASTNode[] children = getNode().getChildren(TokenSet.create(LapgElementTypes.GRAMMAR));
		if(children != null && children.length == 1) {
			return (LpsGrammar) children[0].getPsi();
		}
		return null;
	}

	public String toString() {
		return "LapgFile(syntax):" + getName();
	}
}
