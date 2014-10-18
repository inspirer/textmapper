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
package org.textmapper.idea.watcher;

import com.intellij.ide.macro.FileDirMacro;
import com.intellij.ide.macro.FileNameMacro;
import com.intellij.ide.macro.FileNameWithoutExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer;
import com.intellij.plugins.watcher.model.TaskOptions;
import com.intellij.psi.PsiBundle;
import com.intellij.psi.PsiFile;
import com.intellij.tools.FilterInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;

public class TmTaskConsumer extends BackgroundTaskConsumer {
	public TmTaskConsumer() {
	}

	public boolean isAvailable(PsiFile file) {
		return file instanceof TMPsiFile;
	}

	@NotNull
	public TaskOptions getOptionsTemplate() {
		TaskOptions options = new TaskOptions();
		options.setName("Textmapper");
		options.setDescription("Compiles .tm files into .js files");
		options.setFileExtension("tm");
		options.setScopeName(PsiBundle.message("psi.search.scope.project", new Object[0]));
		options.setArguments("$" + (new FileNameMacro()).getName() + "$");
		options.setWorkingDir("$" + (new FileDirMacro()).getName() + "$");
		options.setOutputFromStdout(false);
		options.setTrackOnlyRoot(false);
		options.setOutput("$" + (new FileNameWithoutExtension()).getName() + "$.js");
		options.setOutputFilters(getFilters());
		return options;
	}

	public void additionalConfiguration(@NotNull Project project, @Nullable PsiFile file, @NotNull TaskOptions options) {
		super.additionalConfiguration(project, file, options);
		options.setProgram(findExecutableInPath(SystemInfo.isWindows ? "textmapper.cmd" : "textmapper"));
	}

	public static FilterInfo[] getFilters() {
		return new FilterInfo[]{new FilterInfo(getPattern(), "Textmapper", "Textmapper error format")};
	}

	public static String getPattern() {
		return "$MESSAGE$$FILE_PATH$?:$LINE$:$COLUMN$";
	}
}
