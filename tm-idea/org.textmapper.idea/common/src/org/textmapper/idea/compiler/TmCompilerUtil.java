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

import org.jetbrains.annotations.NonNls;
import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.tool.common.FileBasedStrategy;
import org.textmapper.tool.gen.LapgGenerator;
import org.textmapper.tool.gen.LapgOptions;
import org.textmapper.tool.parser.TMTree.TextSource;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * evgeny, 11/27/12
 */
public class TmCompilerUtil {

	@NonNls
	public static final String BUILDER_ID = "Textmapper";

	public static boolean compileFile(TmCompilerTask task, TmCompilerContext context) {
		context.reportProgress("Processing " + task.getFile().getName());

		TmProcessingStatus status = context.createProcessingStatus();
		LapgOptions options = new LapgOptions();
		task.fillOptions(options);
		LapgSyntaxBuilder builder = new LapgSyntaxBuilder(task.getFile(), task.getFileContent(), options, status);
		boolean success = builder.generate() && !status.hasErrors();

		if (success) {
			File outPath = task.getOutputDir();
			Map<String, String> generatedContent = builder.getGeneratedContent();
			try {
				context.reportProgress("Saving generated files" + task.getFile().getName());

				for (Entry<String, String> entry : generatedContent.entrySet()) {
					final File destFile = new File(outPath, entry.getKey());
					boolean changed = LapgSyntaxBuilder.writeFile(destFile, entry.getValue());
					context.fileCreated(destFile, !changed);
				}


			} catch (IOException e) {
				status.report(ProcessingStatus.KIND_ERROR, e.toString());
				success = false;
			}
		}
		return success;
	}

	public static void validateFile(TmCompilerTask task, TmProcessingStatus status) {
		LapgOptions options = new LapgOptions();
		task.fillOptions(options);
		TextSource input = new TextSource(task.getFile().getPath(), task.getFileContent().toCharArray(), 1);
		new LapgGenerator(options, status, new FileBasedStrategy(null) {
			@Override
			public void createFile(String name, String contents, ProcessingStatus status) {
				// ignore
				// TODO throw new UnsupportedOperationException();
			}
		}).compileGrammar(input);
	}

}
