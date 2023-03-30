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
package org.textmapper.idea.lang.syntax.parser;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.TMFileType;
import org.textmapper.idea.lang.syntax.psi.TmGrammar;

public class TMPsiFile extends PsiFileImpl {
	protected TMPsiFile(FileViewProvider viewProvider) {
		super(TextmapperElementTypes.FILE, TextmapperElementTypes.FILE, viewProvider);
	}

	@NotNull
	public FileType getFileType() {
		return TMFileType.INSTANCE;
	}

	public void accept(@NotNull PsiElementVisitor visitor) {
		visitor.visitFile(this);
	}

	public TmGrammar getGrammar() {
		return PsiTreeUtil.getChildOfType(this, TmGrammar.class);
	}

	public String toString() {
		return "TMFile(syntax):" + getName();
	}
}
