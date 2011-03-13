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

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.impl.javaCompiler.OutputItemImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Chunk;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.idea.LapgBundle;
import org.textway.lapg.idea.file.LapgFileType;
import org.textway.lapg.idea.parser.LapgFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class LapgCompiler implements TranslatingCompiler {

	private static final FileTypeManager FILE_TYPE_MANAGER = FileTypeManager.getInstance();

	private final Project project;

	public LapgCompiler(Project project) {
		this.project = project;
	}

	@NotNull
	public String getDescription() {
		return LapgBundle.message("compiler.description");
	}

	public boolean isCompilableFile(final VirtualFile file, CompileContext context) {
		final FileType fileType = FILE_TYPE_MANAGER.getFileTypeByFile(file);
		PsiFile psi = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
			public PsiFile compute() {
				return PsiManager.getInstance(project).findFile(file);
			}
		});

		return fileType.equals(LapgFileType.LAPG_FILE_TYPE) && psi instanceof LapgFile;
	}


	public void compile(CompileContext context, Chunk<Module> moduleChunk, VirtualFile[] files, OutputSink sink) {
		ProgressIndicator progressIndicator = context.getProgressIndicator();
		progressIndicator.setText("Lapg Compiler..");
		final List<File> filesToRefresh = new ArrayList<File>();
		final Map<String, Collection<OutputItem>> outputs = new HashMap<String, Collection<OutputItem>>();
		for (VirtualFile file : files) {
			progressIndicator.checkCanceled();
			progressIndicator.setText2(file.getName());
			VirtualFile outputDir = file.getParent();
			if (!outputDir.isDirectory() || !outputDir.isWritable()) {
				// TODO ...
				continue;
			}

			IdeaProcessingStatus status = new IdeaProcessingStatus(file, context);
			LapgSyntaxBuilder builder = new LapgSyntaxBuilder(file, status);
			boolean success = builder.generate() && !status.hasErrors();

			if (success) {
				String outPath = outputDir.getPath();
				Map<String, String> generatedContent = builder.getGeneratedContent();
				try {

					for (Map.Entry<String, String> entry : generatedContent.entrySet()) {
						final File destFile = new File(outPath, entry.getKey());
						File pf = destFile.getParentFile();
						if (!pf.exists() && !pf.mkdirs()) {
							throw new IOException("cannot create folders for `" + pf.getPath() + "'");
						}
						String content = entry.getValue();
						OutputStream os = new FileOutputStream(destFile);
						os.write(content.getBytes("utf8"));
						os.close();
						filesToRefresh.add(destFile);

						// report file
						String outputDirPath = destFile.getParent();
						Collection<OutputItem> collection = outputs.get(outputDirPath);
						if (collection == null) {
							collection = new ArrayList<OutputItem>();
							outputs.put(outputDirPath, collection);
						}
						collection.add(new OutputItemImpl(FileUtil.toSystemIndependentName(destFile.getPath()), file));
					}


				} catch (IOException e) {
					context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, 0, 0);
					success = false;
				}
			}
			if (!success) {
				// recompile later
				sink.add(outputDir.getPath(), Collections.<OutputItem>emptyList(), new VirtualFile[]{file});
			}
		}
		CompilerUtil.refreshIOFiles(filesToRefresh);
		for (Map.Entry<String, Collection<OutputItem>> entry : outputs.entrySet()) {
			sink.add(entry.getKey(), entry.getValue(), VirtualFile.EMPTY_ARRAY);
		}
	}

	public boolean validateConfiguration(CompileScope compileScope) {
		VirtualFile[] files = compileScope.getFiles(LapgFileType.LAPG_FILE_TYPE, true);
		if (files.length == 0) return true;

		// TODO check configuration
		return true;
	}

	private static class IdeaProcessingStatus implements ProcessingStatus {
		private String fileUrl;
		private CompileContext compileContext;
		private boolean hasErrors = false;

		private IdeaProcessingStatus(VirtualFile file, CompileContext compileContext) {
			this.fileUrl = file.getUrl();
			this.compileContext = compileContext;
		}

		public boolean hasErrors() {
			return hasErrors;
		}

		public void report(int kind, String message, SourceElement... anchors) {
			if (kind <= KIND_ERROR) {
				hasErrors = true;
			}
			SourceElement anchor = anchors != null && anchors.length >= 1 ? anchors[0] : null;
			if (anchor == null) {
				compileContext.addMessage(toIdeaKind(kind), message, fileUrl, 1, 1);
			} else {
				compileContext.addMessage(toIdeaKind(kind), message, anchor.getResourceName(), anchor.getLine(), 1);
			}
		}

		public void report(String message, Throwable th) {
			hasErrors = true;
			compileContext.addMessage(CompilerMessageCategory.ERROR, message, fileUrl, 1, 1);
		}

		public void report(ParserConflict conflict) {
			if(conflict.getKind() != ParserConflict.FIXED) {
				report(KIND_ERROR, conflict.getText(), conflict.getRules());
			}
		}

		public void debug(String info) {
			// ignore
		}

		public boolean isDebugMode() {
			return false;
		}

		public boolean isAnalysisMode() {
			return false;
		}

		private CompilerMessageCategory toIdeaKind(int kind) {
			switch (kind) {
				case KIND_FATAL:
				case KIND_ERROR:
					return CompilerMessageCategory.ERROR;
				case KIND_WARN:
					return CompilerMessageCategory.WARNING;
				default:
					return CompilerMessageCategory.INFORMATION;
			}
		}
	}
}
