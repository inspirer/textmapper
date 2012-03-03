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
package org.textway.lapg.idea.lang.templates.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.templates.lexer.LtplTokenTypes;

/**
 * evgeny, 3/3/12
 */
public class LtplParser implements PsiParser {
	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		new Parser(builder).parseBundle();
		file.done(root);
		return builder.getTreeBuilt();
	}

	private static class Parser implements LtplTokenTypes, LtplElementTypes {

		private final PsiBuilder myBuilder;

		public Parser(PsiBuilder myBuilder) {
			this.myBuilder = myBuilder;
		}

		public void parseBundle() {
			Marker bundle = myBuilder.mark();

			while (!myBuilder.eof()) {
				next();
			}
			bundle.done(BUNDLE);
		}

		private void next() {
			myBuilder.advanceLexer();
		}
	}
}
