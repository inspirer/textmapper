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

import com.intellij.ide.structureView.StructureViewModelBase;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;
import org.textmapper.idea.lang.syntax.psi.TmElement;

/**
 * evgeny, 8/11/12
 */
public class TMStructureViewModel extends StructureViewModelBase {
	public TMStructureViewModel(@NotNull TMPsiFile file) {
		super(file, new TMStructureViewElement(file));
		withSuitableClasses(TmElement.class);
	}
}
