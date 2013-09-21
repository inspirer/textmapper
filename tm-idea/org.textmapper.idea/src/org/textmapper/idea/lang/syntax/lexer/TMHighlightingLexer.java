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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.lexer.LayeredLexer;
import com.intellij.psi.tree.IElementType;
import org.textmapper.idea.lang.regex.lexer.RegexLexerAdapter;
import org.textmapper.idea.lang.templates.lexer.LtplLexerAdapter;

/**
 * evgeny, 3/4/12
 */
public class TMHighlightingLexer extends LayeredLexer {

	public TMHighlightingLexer() {
		super(new TMLexerAdapter());
		registerSelfStoppingLayer(new LtplLexerAdapter(),
				new IElementType[]{TMTokenTypes.TEMPLATES}, IElementType.EMPTY_ARRAY);
		registerSelfStoppingLayer(new LtplLexerAdapter(),
				new IElementType[]{TMTokenTypes.TOKEN_ACTION}, IElementType.EMPTY_ARRAY);
		registerSelfStoppingLayer(new RegexLexerAdapter(),
				new IElementType[]{TMTokenTypes.REGEXP}, IElementType.EMPTY_ARRAY);

	}
}
