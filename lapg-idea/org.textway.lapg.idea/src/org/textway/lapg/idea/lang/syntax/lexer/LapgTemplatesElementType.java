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
package org.textway.lapg.idea.lang.syntax.lexer;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.syntax.psi.LpsTemplates;
import org.textway.lapg.idea.lang.templates.LtplFileType;

/**
 * evgeny, 3/4/12
 */
public class LapgTemplatesElementType extends ILazyParseableElementType {

	private int symbol;
	private boolean contentOnly;

	public LapgTemplatesElementType(int symbol, boolean contentOnly, @NotNull String debugName) {
		super(debugName, LtplFileType.LTPL_LANGUAGE);
		this.symbol = symbol;
		this.contentOnly = contentOnly;
	}

	public int getSymbol() {
		return symbol;
	}

	@Override
	public ASTNode createNode(CharSequence text) {
		return new LpsTemplates(this, text);
	}

	@Override
	public String toString() {
		return "[lapg]" + super.toString();
	}
}
