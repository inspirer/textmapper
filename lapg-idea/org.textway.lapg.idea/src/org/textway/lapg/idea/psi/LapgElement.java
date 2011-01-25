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

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.file.LapgFileType;

public class LapgElement extends ASTWrapperPsiElement {

	IElementType type;

	public LapgElement(@NotNull ASTNode node) {
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
