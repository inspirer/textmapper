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
package org.textway.lapg.idea.compiler;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.file.LapgFileType;

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
