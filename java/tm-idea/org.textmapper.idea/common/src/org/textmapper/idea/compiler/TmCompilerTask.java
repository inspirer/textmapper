/**
 * Copyright 2010-2017 Evgeny Gryaznov
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

import org.textmapper.tool.gen.TMOptions;

import java.io.File;

/**
 * evgeny, 11/27/12
 */
public class TmCompilerTask {

	private final File file;
	private final String fileContent;
	private final File outputDir;
	private final boolean verbose;
	private final boolean excludeDefaultTemplates;
	private final String templatesFolder;

	public TmCompilerTask(File file, String fileContent, File outputDir, boolean verbose, boolean excludeDefaultTemplates, String templatesFolder) {
		this.file = file;
		this.fileContent = fileContent;
		this.outputDir = outputDir;
		this.verbose = verbose;
		this.excludeDefaultTemplates = excludeDefaultTemplates;
		this.templatesFolder = templatesFolder;
	}

	public File getFile() {
		return file;
	}

	public String getFileContent() {
		return fileContent;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isExcludeDefaultTemplates() {
		return excludeDefaultTemplates;
	}

	public String getTemplatesFolder() {
		return templatesFolder;
	}

	public void fillOptions(TMOptions options) {
		options.setUseDefaultTemplates(!isExcludeDefaultTemplates());
		if (isVerbose()) {
			options.setDebug(TMOptions.DEBUG_AMBIG);
		}
		String customTemplatesFolder = getTemplatesFolder();
		if (customTemplatesFolder != null && customTemplatesFolder.trim().length() > 0) {
			options.getIncludeFolders().add(customTemplatesFolder);
		}
	}
}
