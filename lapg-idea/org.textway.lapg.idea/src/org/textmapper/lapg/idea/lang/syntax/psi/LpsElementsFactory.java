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
package org.textway.lapg.idea.lang.syntax.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.syntax.LapgFileType;
import org.textway.lapg.idea.lang.syntax.parser.LapgFile;

/**
 * Gryaznov Evgeny, 1/26/11
 */
public class LpsElementsFactory {

	public static LpsSymbol createSymbol(@NotNull Project p, @NotNull String name) throws IncorrectOperationException {
		@NonNls String text = "token: / /\n" + name + " ::= token ;";
		LapgFile aFile = createDummyFile(p, text);
		LpsGrammar grammar = aFile.getGrammar();
		LpsNonTerm[] s = grammar.getNonTerms();
		if (s == null || s.length != 1 || s[0].getNameSymbol() == null) {
			throw new IncorrectOperationException();
		}
		return s[0].getNameSymbol();
	}

	public static LpsReference createReference(@NotNull Project p, @NotNull String name) throws IncorrectOperationException {
		@NonNls String text = name + ": / /\ninput ::= " + name + " ;";
		LapgFile aFile = createDummyFile(p, text);
		LpsGrammar grammar = aFile.getGrammar();
		LpsNonTerm[] s = grammar.getNonTerms();
		if (s == null || s.length != 1) {
			throw new IncorrectOperationException();
		}

		LpsRule[] rules = s[0].getRules();
		if (rules == null || rules.length != 1) {
			throw new IncorrectOperationException();
		}

		LpsReference[] references = rules[0].getRuleRefs();
		if (references == null || references.length != 1) {
			throw new IncorrectOperationException();
		}
		return references[0];
	}

	private static LapgFile createDummyFile(Project p, String text) {
		String ext = LapgFileType.DEFAULT_EXTENSION;
		@NonNls String fileName = "_Dummy_." + ext;
		FileType type = LapgFileType.LAPG_FILE_TYPE;

		return (LapgFile) PsiFileFactory.getInstance(p).createFileFromText(fileName, type, text);
	}
}
