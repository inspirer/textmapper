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
package org.textmapper.idea.lang.syntax.presentation;

import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProvider;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.util.PsiTreeUtil;
import org.textmapper.idea.lang.syntax.psi.TmLexerState;
import org.textmapper.idea.lang.syntax.psi.TmStartConditions;

import javax.swing.*;

/**
 * Gryaznov Evgeny, 9/16/12
 */
public class LexerStatePresentationProvider implements ItemPresentationProvider<TmLexerState> {
	@Override
	public ItemPresentation getPresentation(final TmLexerState lexerState) {
		return new ColoredItemPresentation() {
			@Override
			public String getPresentableText() {
				return lexerState.getName();
			}

			@Override
			public TextAttributesKey getTextAttributesKey() {
				return null;
			}

			@Override
			public String getLocationString() {
				TmStartConditions conditions = PsiTreeUtil.getParentOfType(lexerState, TmStartConditions.class);
				return conditions == null ? null : "(in " + conditions.getText() + ")";
			}

			@Override
			public Icon getIcon(boolean open) {
				return null;
			}
		};
	}
}
