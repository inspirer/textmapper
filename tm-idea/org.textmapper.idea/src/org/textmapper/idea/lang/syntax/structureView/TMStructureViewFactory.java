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
package org.textmapper.idea.lang.syntax.structureView;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;

/**
 * evgeny, 8/11/12
 */
public class TMStructureViewFactory implements PsiStructureViewFactory {

	@Override
	public StructureViewBuilder getStructureViewBuilder(PsiFile psiFile) {
		if (psiFile instanceof TMPsiFile) {
			final TMPsiFile file = (TMPsiFile) psiFile;

			return new TreeBasedStructureViewBuilder() {
				@NotNull
				@Override
				public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
					return new TMStructureViewModel(file);
				}
			};
		}

		return null;
	}
}
