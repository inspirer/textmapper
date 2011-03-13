/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.actions;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.LapgBundle;
import org.textway.lapg.idea.LapgIcons;
import org.textway.lapg.idea.file.LapgFileType;
import org.textway.lapg.idea.lexer.LapgTokenTypes;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class NewLapgGrammar extends CreateElementActionBase {

	public NewLapgGrammar() {
		super(LapgBundle.message("newfile.action.text"), LapgBundle.message("newfile.action.description"), LapgIcons.LAPG_ICON);
	}

	@NotNull
	@Override
	protected PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
		MyInputValidator validator = new MyInputValidator(project, directory);
		Messages.showInputDialog(project, LapgBundle.message("newfile.dlg.text"), LapgBundle.message("newfile.dlg.title"), Messages.getQuestionIcon(), "", validator);
		return validator.getCreatedElements();
	}

	@Override
	protected void checkBeforeCreate(String newName, PsiDirectory directory) throws IncorrectOperationException {
		checkCreateFile(directory, newName);
	}

	public static void checkCreateFile(@NotNull PsiDirectory directory, String name) throws IncorrectOperationException {
		if (!StringUtil.isJavaIdentifier(name)) {
			throw new IncorrectOperationException(LapgBundle.message("error.should.be.identifier", name));
		}

		String fileName = name + "." + LapgFileType.DEFAULT_EXTENSION;
		directory.checkCreateFile(fileName);
	}

	@NotNull
	@Override
	protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
		PsiFile file = LapgTemplatesFactory.createFromTemplate(directory, newName, newName + "." + LapgFileType.DEFAULT_EXTENSION, LapgTemplatesFactory.GRAMMAR_FILE);
		PsiElement lastChild = file.getLastChild();
		final Project project = directory.getProject();
		if (lastChild != null && lastChild.getNode() != null && lastChild.getNode().getElementType() != LapgTokenTypes.WHITESPACE) {
			file.add(createWhiteSpace(project));
		}
		file.add(createWhiteSpace(project));
		PsiElement child = file.getLastChild();
		return child != null ? new PsiElement[]{file, child} : new PsiElement[]{file};
	}

	@Override
	protected String getErrorTitle() {
		return CommonBundle.getErrorTitle();
	}

	@Override
	protected String getCommandName() {
		return LapgBundle.message("newfile.command");
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName) {
		return LapgBundle.message("newfile.action.text");
	}

	private static PsiElement createWhiteSpace(Project project) {
		PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy." + LapgFileType.DEFAULT_EXTENSION, "\n");
		return dummyFile.getFirstChild();
	}
}
