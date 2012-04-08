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
package org.textway.lapg.idea.lang.templates.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textway.lapg.idea.lang.templates.LtplFileType;
import org.textway.lapg.idea.lang.templates.lexer.LtplElementType;
import org.textway.templates.ast.TemplatesParser.Tokens;

/**
 * evgeny, 3/3/12
 */
public interface LtplElementTypes {
	final IFileElementType FILE = new IFileElementType(LtplFileType.LTPL_LANGUAGE);

	public static final IElementType BUNDLE = new LtplElementType(Tokens.input, "bundle");
	public static final IElementType TEMPLATE = new LtplElementType(Tokens.definition, "template");
	public static final IElementType QUERY = new LtplElementType(Tokens.query_def, "query");
}
