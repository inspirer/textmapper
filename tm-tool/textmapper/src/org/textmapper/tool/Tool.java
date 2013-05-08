/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool;

import org.textmapper.lapg.common.AbstractProcessingStatus;
import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.common.FileBasedStrategy;
import org.textmapper.tool.common.GeneratedFile;
import org.textmapper.tool.gen.LapgGenerator;
import org.textmapper.tool.gen.LapgOptions;
import org.textmapper.tool.parser.TMTree.TextSource;

import java.io.*;

/**
 * Main console entry point for Textmapper engine.
 */
public class Tool {

	public static final String VERSION = "0.9.1/java";
	public static final String BUILD = "2013";

	public static final String HELP_MESSAGE =
			"textmapper - Lexer and Parser generator\n" +
					"usage: textmapper [OPTIONS]... [inputfile]\n" +
					"       textmapper [-h|-v]\n" +
					"\n" +
					"Options:\n" +
					LapgOptions.HELP_OPTIONS +
					"\n" +
					"Operations:\n" +
					"  -h,  --help                    display this help\n" +
					"  -v,  --version                 version information\n" +
					"\n" +
					"Defaults:\n" +
					"  inputfile = .tm file in the current directory (if single)\n";

	public static final String VERSION_MESSAGE =
			"textmapper v" + VERSION + " build " + BUILD + "\n" +
					"Evgeny Gryaznov, 2002-2013, egryaznov@gmail.com\n";


	public static void main(String[] args) {
		if (args.length >= 1 && args[0] != null) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println(HELP_MESSAGE);
				return;
			}
			if (args[0].equals("-v") || args[0].equals("--version")) {
				System.out.println(VERSION_MESSAGE);
				return;
			}
		}

		LapgOptions options = LapgOptions.parseArguments(args, System.err);
		if (options == null) {
			System.err.println("Try 'textmapper --help' for more information.");
			System.exit(1);
			return;
		}

		if (options.getInput() == null) {
			File[] grammars = new File(".").listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.getName().endsWith(".tm");
				}
			});
			if (grammars == null || grammars.length != 1) {
				if (grammars == null || grammars.length == 0) {
					System.err.println("textmapper: no syntax files found, please specify");
				} else {
					System.err.println("textmapper: " + grammars.length + " syntax files found, please specify");
				}
				System.err.println("Try 'textmapper --help' for more information.");
				System.exit(1);
				return;
			} else {
				options.setInput(grammars[0].getName());
			}
		}

		InputStream stream;
		assert options.getInput() != null;
		if (!options.getInput().startsWith("-")) {
			try {
				stream = new FileInputStream(options.getInput());
			} catch (FileNotFoundException ex) {
				System.err.println("textmapper: file not found: " + options.getInput());
				System.exit(1);
				return;
			}
		} else {
			stream = System.in;
		}
		String contents = FileUtil.getFileContents(stream, FileUtil.DEFAULT_ENCODING);
		if (contents == null) {
			System.err.println("textmapper: cannot read file: " + options.getInput());
			System.exit(1);
			return;
		}

		ConsoleStatus status = createStatus(options.getDebug());
		boolean success;
		try {
			TextSource input = new TextSource(options.getInput(), contents.toCharArray(), 1);
			FileBasedStrategy strategy = new FileBasedStrategy(null);

			success = new LapgGenerator(options, status, strategy).compileGrammar(input);
		} finally {
			status.dispose();
		}
		if (!success) {
			System.exit(1);
		}
	}

	private static ConsoleStatus createStatus(int debuglev) {
		new File(ConsoleStatus.OUT_ERRORS).delete();
		new File(ConsoleStatus.OUT_TABLES).delete();
		return new ConsoleStatus(debuglev);
	}

	private static class ConsoleStatus extends AbstractProcessingStatus {

		static final String OUT_ERRORS = "errors";
		static final String OUT_TABLES = "tables";

		private PrintStream debug, warn;

		public ConsoleStatus(int debuglev) {
			super(debuglev >= LapgOptions.DEBUG_TABLES, debuglev >= LapgOptions.DEBUG_AMBIG);
			debug = null;
			warn = null;
		}

		private PrintStream openFile(String name) {
			try {
				return new PrintStream(new FileOutputStream(name));
			} catch (FileNotFoundException ex) {
				handle(KIND_ERROR, "textmapper: IO error: " + ex.getMessage());
				return System.err;
			}
		}

		@Override
		public void handle(int kind, String text) {
			if (kind == KIND_ERROR || kind == KIND_FATAL) {
				System.err.print(text);
			} else if (kind == KIND_INFO) {
				System.out.print(text);
			} else if (kind == KIND_DEBUG) {
				if (!isDebugMode()) {
					return;
				}
				if (debug == null) {
					debug = openFile(OUT_TABLES);
				}
				debug.print(FileUtil.fixLineSeparators(text, GeneratedFile.NL));
			} else if (kind == KIND_WARN) {
				if (!isAnalysisMode()) {
					return;
				}
				if (warn == null) {
					warn = openFile(OUT_ERRORS);
				}
				warn.print(FileUtil.fixLineSeparators(text, GeneratedFile.NL));
			}
		}

		@Override
		public void report(String message, Throwable th) {
			System.err.print(message + "\n");
			if (th != null && isDebugMode()) {
				th.printStackTrace(System.err);
			}
		}

		public void dispose() {
			if (debug != null) {
				debug.close();
				debug = null;
			}
			if (warn != null) {
				warn.close();
				warn = null;
			}
		}
	}
}
