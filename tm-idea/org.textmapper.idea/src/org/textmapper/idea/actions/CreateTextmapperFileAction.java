/**
 * Copyright (c) 2010-2014 Evgeny Gryaznov
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

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.TextmapperBundle;

public class CreateTextmapperFileAction extends CreateFileFromTemplateAction implements DumbAware {

	public CreateTextmapperFileAction() {
		super(TextmapperBundle.message("newfile.action.text"), TextmapperBundle.message("newfile.action.description"), TMIcons.TM_ICON);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle(TextmapperBundle.message("newfile.action.text"));
		for (TemplatesHandler handler : TemplatesHandler.EP_NAME.getExtensions()) {
			handler.addTemplates(builder);
		}
		builder.addKind("generates Javascript", StdFileTypes.JS.getIcon(), "GrammarForJS.tm");
		builder.setValidator(new InputValidatorEx() {
			@Nullable
			@Override
			public String getErrorText(String inputString) {
				return null;
			}

			@Override
			public boolean checkInput(String inputString) {
				return StringUtil.isJavaIdentifier(inputString);
			}

			@Override
			public boolean canClose(String inputString) {
				return checkInput(inputString);
			}
		});
	}

	@Override
	protected String getActionName(PsiDirectory directory, String newName, String templateName) {
		return TextmapperBundle.message("newfile.action.text");
	}
}