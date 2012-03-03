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
package org.textway.lapg.idea.lang.templates.lexer;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.templates.LtplFileType;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public class LtplElementType extends IElementType {
	private final int symbol;

	public LtplElementType(int symbol, @NotNull String debugName) {
		super(debugName, LtplFileType.LTPL_LANGUAGE);
		this.symbol = symbol;
	}

	public int getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return "[lapg templates]" + super.toString();
	}
}

