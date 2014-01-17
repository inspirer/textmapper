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
package org.textmapper.idea.actions;

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
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.TextmapperBundle;
import org.textmapper.idea.lang.syntax.TMFileType;
import org.textmapper.idea.lang.syntax.lexer.TMTokenTypes;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class NewTMGrammar extends CreateElementActionBase {

	public NewTMGrammar() {
		super(TextmapperBundle.message("newfile.action.text"), TextmapperBundle.message("newfile.action.description"), TMIcons.TM_ICON);
	}

	@NotNull
	@Override
	protected PsiElement[] invokeDialog(final Project project, final PsiDirectory directory) {
		final MyInputValidator validator = new MyInputValidator(project, directory) {
			@Override
			public boolean checkInput(String inputString) {
				return StringUtil.isJavaIdentifier(inputString);
			}
		};
		Messages.showInputDialog(project, TextmapperBundle.message("newfile.dlg.text"), TextmapperBundle.message("newfile.dlg.title"), Messages.getQuestionIcon(), "", validator);
		return validator.getCreatedElements();
	}

	public static void checkCreateFile(@NotNull PsiDirectory directory, String name) throws IncorrectOperationException {
		if (!StringUtil.isJavaIdentifier(name)) {
			throw new IncorrectOperationException(TextmapperBundle.message("error.should.be.identifier", name));
		}

		String fileName	 = name + "." + TMFileType.DEFAULT_EXTENSION;
		directory.checkCreateFile(fileName);
	}

	@NotNull
	@Override
	protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
		checkCreateFile(directory, newName);
		PsiFile file = TMTemplatesFactory.createFromTemplate(directory, newName, newName + "." + TMFileType.DEFAULT_EXTENSION, TMTemplatesFactory.GRAMMAR_FILE);
		PsiElement lastChild = file.getLastChild();
		final Project project = directory.getProject();
		if (lastChild != null && lastChild.getNode() != null && lastChild.getNode().getElementType() != TMTokenTypes.WHITESPACE) {
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
		return TextmapperBundle.message("newfile.command");
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName) {
		return TextmapperBundle.message("newfile.action.text");
	}

	private static PsiElement createWhiteSpace(Project project) {
		PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy." + TMFileType.DEFAULT_EXTENSION, TMFileType.INSTANCE, "\n");
		return dummyFile.getFirstChild();
	}
}
