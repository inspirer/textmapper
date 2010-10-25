/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.common.FileBasedStrategy;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.gen.LapgGenerator;
import org.textway.lapg.gen.LapgOptions;
import org.textway.lapg.parser.LapgTree.TextSource;

/**
 * Main console entry point for Lapg engine.
 */
public class Lapg {

	public static final String VERSION = "1.4.0/java";
	public static final String BUILD = "2010";

	public static final String HELP_MESSAGE =
		"lapg - Lexer and Parser generator\n"+
		"usage: lapg [OPTIONS]... [inputfile]\n"+
		"       lapg [-h|-v]\n"+
		"\n"+
		"Options:\n"+
		LapgOptions.HELP_OPTIONS+
		"\n"+
		"Operations:\n"+
		"  -h,  --help                    display this help\n"+
		"  -v,  --version                 version information\n"+
		"\n"+
		"Defaults:\n"+
		"  inputfile = .s file in the current directory (if single)\n";

	public static final String VERSION_MESSAGE =
		"lapg v" + VERSION + " build " + BUILD + "\n" +
		"Evgeny Gryaznov, 2002-10, egryaznov@gmail.com\n";


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
			System.err.println("Try 'lapg --help' for more information.");
			System.exit(1);
			return;
		}

		if(options.getInput() == null) {
			File[] grammars = new File(".").listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.getName().endsWith(".s");
				}
			});
			if(grammars == null || grammars.length != 1) {
				if(grammars == null || grammars.length == 0) {
					System.err.println("lapg: no syntax files found, please specify");
				} else {
					System.err.println("lapg: " + grammars.length + " syntax files found, please specify");
				}
				System.err.println("Try 'lapg --help' for more information.");
				System.exit(1);
				return;
			} else {
				options.setInput(grammars[0].getName());
			}
		}

		InputStream stream;
		if (options.getInput() != null && !options.getInput().startsWith("-")) {
			try {
				stream = new FileInputStream(options.getInput());
			} catch (FileNotFoundException ex) {
				System.err.println("lapg: file not found: " + options.getInput());
				System.exit(1);
				return;
			}
		} else {
			stream = System.in;
		}
		String contents = FileUtil.getFileContents(stream, FileUtil.DEFAULT_ENCODING);
		if(contents == null) {
			System.err.println("lapg: cannot read file: " + options.getInput());
			System.exit(1);
			return;
		}

		ConsoleStatus status = createStatus(options.getDebug());
		boolean success;
		try {
			TextSource input = new TextSource(options.getInput(), contents.toCharArray(), 1);
			FileBasedStrategy strategy = new FileBasedStrategy();

			success = new LapgGenerator(options, status, strategy).compileGrammar(input);
		} finally {
			status.dispose();
		}
		if(!success) {
			System.exit(1);
		}
	}

	private static ConsoleStatus createStatus(int debuglev) {
		new File(ConsoleStatus.OUT_ERRORS).delete();
		new File(ConsoleStatus.OUT_TABLES).delete();
		return new ConsoleStatus(debuglev);
	}

	private static class ConsoleStatus implements ProcessingStatus {

		static final String OUT_ERRORS = "errors";
		static final String OUT_TABLES = "tables";

		private PrintStream debug, warn;
		private final int debuglev;

		public ConsoleStatus(int debuglev) {
			this.debuglev = debuglev;
			this.debug = null;
			this.warn = null;
		}

		private PrintStream openFile(String name) {
			try {
				return new PrintStream(new FileOutputStream(name));
			} catch (FileNotFoundException ex) {
				error("lapg: IO error: " + ex.getMessage());
				return System.err;
			}
		}

		public void error(String error) {
			System.err.print(error);
		}

		public void info(String info) {
			System.out.print(info);
		}

		public void trace(Throwable ex) {
			ex.printStackTrace(System.err);
		}

		public void debug(String info) {
			if (debuglev < 2) {
				return;
			}
			if (debug == null) {
				debug = openFile(OUT_TABLES);
			}
			debug.print(info);
		}

		public void warn(String warning) {
			if (debuglev < 1) {
				return;
			}
			if (warn == null) {
				warn = openFile(OUT_ERRORS);
			}
			warn.print(warning);
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

		public void report(String message, Throwable th) {
			error(message + "\n");
			if(th != null) {
				trace(th);
			}
		}

		public void report(int kind, String message, SourceElement ...anchors) {
			SourceElement anchor = anchors != null && anchors.length > 0 ? anchors[0] : null;
			switch(kind) {
			case KIND_FATAL:
			case KIND_ERROR:
				if(anchor != null && anchor.getResourceName() != null) {
					error(anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				error(message + "\n");
				break;
			case KIND_WARN:
				if(anchor != null && anchor.getResourceName() != null) {
					warn(anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				warn(message + "\n");
				break;
			case KIND_INFO:
				if(anchor != null && anchor.getResourceName() != null) {
					info(anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				info(message + "\n");
				break;
			}
		}

		public void report(ParserConflict conflict) {
			Rule rule = conflict.getRules()[0];
			if(conflict.getKind() == ParserConflict.FIXED) {
				if(isAnalysisMode()) {
					report(KIND_WARN, conflict.getText(), rule);
				}
			} else {
				report(KIND_ERROR, conflict.getText(), rule);
			}
		}

		public boolean isDebugMode() {
			return debuglev >= 2;
		}

		public boolean isAnalysisMode() {
			return debuglev >= 1;
		}
	}
}
