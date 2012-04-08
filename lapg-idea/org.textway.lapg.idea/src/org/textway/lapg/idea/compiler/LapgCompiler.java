/**
 * Copyright (c) 2010-2011 Evgeny Gryaznov
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
package org.textway.lapg.idea.compiler;

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.impl.javaCompiler.OutputItemImpl;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Chunk;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.api.DerivedSourceElement;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.idea.LapgBundle;
import org.textway.lapg.idea.facet.LapgConfigurationBean;
import org.textway.lapg.idea.facet.LapgFacet;
import org.textway.lapg.idea.facet.LapgFacetType;
import org.textway.lapg.idea.lang.syntax.LapgFileType;
import org.textway.lapg.idea.lang.syntax.parser.LapgFile;
import org.textway.lapg.api.TextSourceElement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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
		Map<Module, List<VirtualFile>> mapToFiles = CompilerUtil.buildModuleToFilesMap(context, files);

		for (Entry<Module, List<VirtualFile>> e : mapToFiles.entrySet()) {
			compileModule(context, e.getKey(), e.getValue(), sink);
		}
	}

	public void compileModule(CompileContext context, Module module, Collection<VirtualFile> files, OutputSink sink) {
		LapgFacet facet = FacetManager.getInstance(module).getFacetByType(LapgFacetType.ID);
		if (facet == null) {
			return;
		}

		final List<File> filesToRefresh = new ArrayList<File>();
		final Map<String, Collection<OutputItem>> outputs = new HashMap<String, Collection<OutputItem>>();
		for (VirtualFile file : files) {
			context.getProgressIndicator().checkCanceled();
			context.getProgressIndicator().setText("Compiling " + file.getName());
			compileFile(context, file, sink, filesToRefresh, outputs);
		}
		CompilerUtil.refreshIOFiles(filesToRefresh);
		for (Map.Entry<String, Collection<OutputItem>> entry : outputs.entrySet()) {
			sink.add(entry.getKey(), entry.getValue(), VirtualFile.EMPTY_ARRAY);
		}
	}

	private void compileFile(CompileContext context, VirtualFile file, OutputSink sink, List<File> filesToRefresh, Map<String, Collection<OutputItem>> outputs) {
		VirtualFile outputDir = file.getParent();
		if (!outputDir.isDirectory() || !outputDir.isWritable()) {
			// TODO ...
			return;
		}

		IdeaProcessingStatus status = new IdeaProcessingStatus(file, context);
		LapgSyntaxBuilder builder = new LapgSyntaxBuilder(file, status);
		boolean success = builder.generate() && !status.hasErrors();

		if (success) {
			String outPath = outputDir.getPath();
			Map<String, String> generatedContent = builder.getGeneratedContent();
			try {
				context.getProgressIndicator().setText("Saving " + file.getName());

				for (Entry<String, String> entry : generatedContent.entrySet()) {
					final File destFile = new File(outPath, entry.getKey());
					LapgSyntaxBuilder.writeFile(destFile, entry.getValue());
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

	public boolean validateConfiguration(CompileScope compileScope) {
		VirtualFile[] files = compileScope.getFiles(LapgFileType.LAPG_FILE_TYPE, true);
		if (files.length == 0) return true;

		final Module[] modules = compileScope.getAffectedModules();
		for (final Module module : modules) {
			LapgFacet facet = FacetManager.getInstance(module).getFacetByType(LapgFacetType.ID);
			if (facet != null) {
				LapgConfigurationBean configuration = facet.getConfiguration().getState();
				boolean hasTemplatesFolder = configuration.templatesFolder.trim().length() > 0;

				if (configuration.excludeDefaultTemplates && !hasTemplatesFolder) {
					Messages.showErrorDialog(module.getProject(), LapgBundle.message("compiler.facetproblem.no_templates", module.getName()),
							LapgBundle.message("compiler.facetproblem.title"));

					ModulesConfigurator.showFacetSettingsDialog(facet, "Lapg");
					return false;
				}
			}
		}
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
			while (anchor instanceof DerivedSourceElement) {
				anchor = ((DerivedSourceElement) anchor).getOrigin();
			}
			if (anchor instanceof TextSourceElement) {
				compileContext.addMessage(toIdeaKind(kind), message, ((TextSourceElement) anchor).getResourceName(), ((TextSourceElement) anchor).getLine(), 1);
			} else {
				compileContext.addMessage(toIdeaKind(kind), message, fileUrl, 1, 1);
			}
		}

		public void report(String message, Throwable th) {
			hasErrors = true;
			compileContext.addMessage(CompilerMessageCategory.ERROR, message, fileUrl, 1, 1);
		}

		public void report(ParserConflict conflict) {
			if (conflict.getKind() != ParserConflict.FIXED) {
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
