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
		LpsSymbol[] s = grammar.getSymbols();
		if (s.length != 2) {
			throw new IncorrectOperationException();
		}
		return s[1];
	}

	public static LpsReference createReference(@NotNull Project p, @NotNull String name) throws IncorrectOperationException {
		@NonNls String text = "aa = " + name + "\n" + name + ": / /\ninput ::= " + name + " ;";
		LapgFile aFile = createDummyFile(p, text);
		LpsGrammar grammar = aFile.getGrammar();

		// TODO fix
		return (LpsReference)grammar.getFirstChild().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild();
	}


	private static LapgFile createDummyFile(Project p, String text) {
		String ext = LapgFileType.DEFAULT_EXTENSION;
		@NonNls String fileName = "_Dummy_." + ext;
		FileType type = LapgFileType.LAPG_FILE_TYPE;

		return (LapgFile) PsiFileFactory.getInstance(p).createFileFromText(fileName, type, text);
	}
}
