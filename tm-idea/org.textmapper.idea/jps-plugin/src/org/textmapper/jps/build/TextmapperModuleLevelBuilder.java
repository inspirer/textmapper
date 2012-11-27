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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.messages.BuildMessage.Kind;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.module.JpsModule;
import org.textmapper.jps.model.JpsTmExtensionService;
import org.textmapper.jps.model.JpsTmModuleExtension;

import java.io.IOException;

/**
 * evgeny, 11/26/12
 */
public class TextmapperModuleLevelBuilder extends ModuleLevelBuilder {

	@NonNls
	private static final String BUILDER_NAME = "Textmapper";

	protected TextmapperModuleLevelBuilder() {
		super(BuilderCategory.SOURCE_PROCESSOR);
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return BUILDER_NAME;
	}

	@Override
	public ExitCode build(CompileContext compileContext,
						  ModuleChunk moduleChunk,
						  DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
						  OutputConsumer outputConsumer) throws ProjectBuildException, IOException {
		boolean doneSomething = false;

		for (final JpsModule module : moduleChunk.getModules()) {
			JpsTmModuleExtension ext = JpsTmExtensionService.getInstance().getExtension(module);
			if (ext == null) {
				compileContext.processMessage(new CompilerMessage(BUILDER_NAME, Kind.INFO, "skipping " + module.getName()));
				continue;
			}
			doneSomething |= processModule(compileContext, dirtyFilesHolder, ext);
		}

		return doneSomething ? ExitCode.OK : ExitCode.NOTHING_DONE;
	}

	private boolean processModule(final CompileContext context,
								  final DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> holder,
								  final JpsTmModuleExtension extension) {

		// TODO
		context.processMessage(new CompilerMessage(BUILDER_NAME, Kind.INFO, "textmapper done for " + extension.getModule().getName() + " (custom folder = " + extension.getCustomTemplatesFolder() + ")"));
		return false;
	}
}
