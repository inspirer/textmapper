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
package org.textmapper.idea.compiler;

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
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Chunk;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.LapgBundle;
import org.textmapper.idea.facet.LapgFacet;
import org.textmapper.idea.facet.LapgFacetType;
import org.textmapper.idea.facet.TmConfigurationBean;
import org.textmapper.idea.lang.syntax.LapgFileType;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;
import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.ParserConflict;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;

import java.io.File;
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

		return fileType.equals(LapgFileType.LAPG_FILE_TYPE) && psi instanceof TMPsiFile;
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
			compileFile(context, file, sink, filesToRefresh, outputs, facet.getConfiguration().getState());
		}
		CompilerUtil.refreshIOFiles(filesToRefresh);
		for (Map.Entry<String, Collection<OutputItem>> entry : outputs.entrySet()) {
			sink.add(entry.getKey(), entry.getValue(), VirtualFile.EMPTY_ARRAY);
		}
	}

	private void compileFile(final CompileContext context, final VirtualFile sourceFile, OutputSink sink, final List<File> filesToRefresh, final Map<String, Collection<OutputItem>> outputs, TmConfigurationBean options) {
		VirtualFile outputDir = sourceFile.getParent();
		if (!outputDir.isDirectory() || !outputDir.isWritable()) {
			// TODO ...
			return;
		}

		boolean success = TmCompilerUtil.compileFile(
				new TmCompilerTask(
						VfsUtil.virtualToIoFile(sourceFile),
						null,
						VfsUtil.virtualToIoFile(outputDir),
						options.verbose, options.excludeDefaultTemplates, options.templatesFolder),
				new TmCompilerContext() {
					@Override
					public TmProcessingStatus createProcessingStatus() {
						return new IdeaProcessingStatus(sourceFile, context);
					}

					@Override
					public void fileCreated(File newFile, boolean isUnchanged) {
						filesToRefresh.add(newFile);

						// report file
						String outputDirPath = newFile.getParent();
						Collection<OutputItem> collection = outputs.get(outputDirPath);
						if (collection == null) {
							collection = new ArrayList<OutputItem>();
							outputs.put(outputDirPath, collection);
						}
						collection.add(new OutputItemImpl(FileUtil.toSystemIndependentName(newFile.getPath()), sourceFile));
					}

					@Override
					public void reportProgress(String message) {
						context.getProgressIndicator().setText(message);
					}
				});

		if (!success) {
			// recompile later
			sink.add(outputDir.getPath(), Collections.<OutputItem>emptyList(), new VirtualFile[]{sourceFile});
		}
	}

	public boolean validateConfiguration(CompileScope compileScope) {
		VirtualFile[] files = compileScope.getFiles(LapgFileType.LAPG_FILE_TYPE, true);
		if (files.length == 0) return true;

		final Module[] modules = compileScope.getAffectedModules();
		for (final Module module : modules) {
			LapgFacet facet = FacetManager.getInstance(module).getFacetByType(LapgFacetType.ID);
			if (facet != null) {
				TmConfigurationBean configuration = facet.getConfiguration().getState();
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

	private static class IdeaProcessingStatus implements TmProcessingStatus {
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
