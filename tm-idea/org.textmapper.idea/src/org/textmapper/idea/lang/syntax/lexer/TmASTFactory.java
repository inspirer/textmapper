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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.lang.DefaultASTFactoryImpl;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Gryaznov Evgeny, 9/21/12
 */
public class TmASTFactory extends DefaultASTFactoryImpl {

	@Override
	@NotNull
	public LeafElement createLeaf(final IElementType type, final CharSequence text) {
		if (TMTokenTypes.comments.contains(type)) {
			return createComment(type, text);
		}

		return new TmToken(type, text);
	}

}
