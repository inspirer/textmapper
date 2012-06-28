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
package org.textmapper.lapg.idea.compiler;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.textmapper.lapg.idea.lang.syntax.LapgFileType;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class LapgCompilerProjectComponent implements ProjectComponent {

	private Project project;

	public LapgCompilerProjectComponent(Project project) {
		this.project = project;
	}

	public void projectOpened() {
		CompilerManager compilerManager = CompilerManager.getInstance(project);
		compilerManager.addCompilableFileType(LapgFileType.LAPG_FILE_TYPE);

		for (LapgCompiler compiler : compilerManager.getCompilers(LapgCompiler.class)) {
		  compilerManager.removeCompiler(compiler);
		}
		HashSet<FileType> inputSet = new HashSet<FileType>(Arrays.asList(LapgFileType.LAPG_FILE_TYPE));
		HashSet<FileType> outputSet = new HashSet<FileType>(Arrays.asList(StdFileTypes.JAVA));
		compilerManager.addTranslatingCompiler(new LapgCompiler(project), inputSet, outputSet);
	}

	public void projectClosed() {
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "Lapg Compiler Component";
	}
}
