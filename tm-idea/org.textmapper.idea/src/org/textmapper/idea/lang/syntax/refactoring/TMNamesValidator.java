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
package org.textmapper.idea.lang.syntax.refactoring;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.Tokens;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * evgeny, 8/13/12
 */
public class TMNamesValidator implements NamesValidator {

	private static final Set<String> keywords = new HashSet<>(Arrays.asList("new", "separator", "true", "false"));

	public boolean isKeyword(String name, Project project) {
		return keywords.contains(name);
	}

	public boolean isIdentifier(String name, Project project) {
		try {
			TMLexer lapgLexer = new ValidatingLexer(name);
			return lapgLexer.next().symbol == Tokens.ID && lapgLexer.next().symbol == Tokens.eoi;
		} catch (Exception ignored) {
		}
		return false;
	}

	private static class ValidatingLexer extends TMLexer {
		public ValidatingLexer(String name) throws java.io.IOException {
			super(name, (message, line, offset, endoffset) -> {
				throw new IllegalArgumentException();
			});
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			super.createToken(token, ruleIndex);
			/* include comments and spaces */
			return true;
		}
	}
}
