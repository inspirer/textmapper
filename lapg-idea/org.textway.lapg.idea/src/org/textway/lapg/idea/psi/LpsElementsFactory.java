/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.file.LapgFileType;
import org.textway.lapg.idea.parser.LapgFile;

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
