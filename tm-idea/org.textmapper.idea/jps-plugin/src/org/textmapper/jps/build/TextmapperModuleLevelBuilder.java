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
package org.textmapper.jps.build;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.messages.BuildMessage.Kind;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.CustomBuilderMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerConfiguration;
import org.textmapper.idea.compiler.*;
import org.textmapper.jps.model.JpsTmExtensionService;
import org.textmapper.jps.model.JpsTmModuleExtension;
import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.ParserConflict;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * evgeny, 11/26/12
 */
public class TextmapperModuleLevelBuilder extends ModuleLevelBuilder {

	private static final String TM_EXTENSION = ".tm";
	private static final FileFilter TM_SOURCES_FILTER =
			SystemInfo.isFileSystemCaseSensitive ?
					new FileFilter() {
						public boolean accept(File file) {
							return file.getPath().endsWith(TM_EXTENSION);
						}
					} :
					new FileFilter() {
						public boolean accept(File file) {
							return StringUtil.endsWithIgnoreCase(file.getPath(), TM_EXTENSION);
						}
					};

	private TmIdeaRefreshComponent refreshComponent = new TmIdeaRefreshComponent();

	protected TextmapperModuleLevelBuilder() {
		super(BuilderCategory.SOURCE_GENERATOR);
	}

	@Override
	public void buildStarted(final CompileContext context) {
		context.addBuildListener(new BuildListener() {
			@Override
			public void filesGenerated(Collection<Pair<String, String>> paths) {
			}

			@Override
			public void filesDeleted(Collection<String> paths) {
				refreshComponent.filesRemoved(paths);
			}
		});
	}

	@Override
	public void buildFinished(CompileContext context) {
		Collection<String> filesToRefresh = refreshComponent.getFilesToRefresh();
		for (String file : filesToRefresh) {
			context.processMessage(new CustomBuilderMessage(TmCompilerUtil.BUILDER_ID, TmBuilderMessages.MSG_CHANGED, file));
		}
		context.processMessage(new CustomBuilderMessage(TmCompilerUtil.BUILDER_ID, TmBuilderMessages.MSG_REFRESH, ""));
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return TmCompilerUtil.BUILDER_ID;
	}

	@Override
	public ExitCode build(final CompileContext compileContext,
						  ModuleChunk moduleChunk,
						  final DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
						  final OutputConsumer outputConsumer) throws ProjectBuildException, IOException {
		ExitCode status = ExitCode.NOTHING_DONE;
		try {
			final List<Pair<TmCompilerTask, ModuleBuildTarget>> toCompile = collectChangedFiles(compileContext, dirtyFilesHolder);
			if (toCompile.isEmpty()) {
				return status;
			}

			for (final Pair<TmCompilerTask, ModuleBuildTarget> entry : toCompile) {
				final TmCompilerTask task = entry.getFirst();
				refreshComponent.addOutputRoot(task.getOutputDir().getPath());
				boolean success = TmCompilerUtil.compileFile(task, new TmCompilerContext() {
					@Override
					public TmProcessingStatus createProcessingStatus() {
						return new JpsTmProcessingStatus(task.getFile(), compileContext);
					}

					@Override
					public void fileCreated(File newFile, boolean isUnchanged) throws IOException {
						final String sourcePath = FileUtil.toSystemIndependentName(task.getFile().getPath());
						if (!isUnchanged) {
							// mark dirty to re-compile
							FSOperations.markDirty(compileContext, newFile);

							// refresh virtual file in IDEA
							refreshComponent.refresh(newFile.getPath());
						}
						outputConsumer.registerOutputFile(entry.getSecond(), newFile, Collections.singletonList(sourcePath));
					}

					@Override
					public void reportProgress(String message) {
						compileContext.processMessage(new ProgressMessage(message));
					}
				});
				if (success) {
					status = ExitCode.OK;
				}
			}
		} catch (Exception ex) {
			throw new ProjectBuildException(ex);
		}

		return status;
	}

	private static List<Pair<TmCompilerTask, ModuleBuildTarget>> collectChangedFiles(CompileContext context,
																					 DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder) throws IOException {
		final JpsJavaCompilerConfiguration configuration = JpsJavaExtensionService.getInstance().getCompilerConfiguration(context.getProjectDescriptor().getProject());
		assert configuration != null;

		final List<Pair<TmCompilerTask, ModuleBuildTarget>> toCompile = new ArrayList<Pair<TmCompilerTask, ModuleBuildTarget>>();
		dirtyFilesHolder.processDirtyFiles(new FileProcessor<JavaSourceRootDescriptor, ModuleBuildTarget>() {
			public boolean apply(ModuleBuildTarget target, File file, JavaSourceRootDescriptor sourceRoot) throws IOException {
				if (TM_SOURCES_FILTER.accept(file) && !configuration.isResourceFile(file, sourceRoot.root)) {
					JpsTmModuleExtension ext = JpsTmExtensionService.getInstance().getExtension(target.getModule());
					if (ext != null) {
						File outputDir = file.getParentFile();
						if (outputDir.isDirectory()) {
							toCompile.add(new Pair<TmCompilerTask, ModuleBuildTarget>(
									new TmCompilerTask(file, null, outputDir, ext.isVerbose(), ext.isExcludeDefaultTemplates(), ext.getCustomTemplatesFolder()),
									target));
						}
					}
				}
				return true;
			}
		});
		return toCompile;
	}

	private static class JpsTmProcessingStatus implements TmProcessingStatus {
		private File originalFile;
		private CompileContext compileContext;
		private boolean hasErrors = false;

		public JpsTmProcessingStatus(File originalFile, CompileContext compileContext) {
			this.compileContext = compileContext;
			this.originalFile = originalFile;
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
				TextSourceElement textElement = (TextSourceElement) anchor;
				compileContext.processMessage(new CompilerMessage(TmCompilerUtil.BUILDER_ID, toIdeaKind(kind),
						message, textElement.getResourceName(),
						textElement.getOffset(), textElement.getEndOffset(), textElement.getOffset(),
						textElement.getLine(), 1 /* TODO */));
			} else {
				compileContext.processMessage(new CompilerMessage(TmCompilerUtil.BUILDER_ID, toIdeaKind(kind),
						message, originalFile.getPath()));
			}
		}

		public void report(String message, Throwable th) {
			hasErrors = true;
			compileContext.processMessage(new CompilerMessage(TmCompilerUtil.BUILDER_ID, Kind.ERROR,
					message + (th != null ? ": " + th.getMessage() : ""), originalFile.getPath()));
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

		private Kind toIdeaKind(int kind) {
			switch (kind) {
				case KIND_FATAL:
				case KIND_ERROR:
					return Kind.ERROR;
				case KIND_WARN:
					return Kind.WARNING;
				default:
					return Kind.INFO;
			}
		}
	}
}
