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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.TMFileType;

public class TMElementType extends IElementType {
	private final int symbol;

	public TMElementType(int symbol, @NotNull String debugName) {
		super(debugName, TMFileType.TM_LANGUAGE);
		this.symbol = symbol;
	}

	public int getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return "[tm] " + super.toString();
	}
}
