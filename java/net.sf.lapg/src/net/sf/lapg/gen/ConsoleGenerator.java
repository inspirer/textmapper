/*************************************************************
 * Copyright (c) 2002-2009 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import net.sf.lapg.INotifier;
import net.sf.lapg.common.FileCreator;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.impl.FolderTemplateLoader;

public class ConsoleGenerator extends AbstractGenerator {

	public ConsoleGenerator(LapgOptions options) {
		super(options);
	}

	@Override
	public void createFile(String name, String contents) {
		try {
			new FileCreator(name, contents, "utf8").create(); // FIXME encoding
		} catch (IOException e) {
			// TODO report
		}
	}

	@Override
	public InputStream openInput(INotifier notifier) {
		InputStream stream;
		if (options.getInput() != null && !options.getInput().startsWith("-")) {
			try {
				stream = new FileInputStream(options.getInput());
			} catch (FileNotFoundException ex) {
				notifier.error("lapg: file not found: " + options.getInput() + "\n");
				return null;
			}
		} else {
			stream = System.in;
		}
		return stream;
	}

	@Override
	protected INotifier createNotifier() {
		new File(ConsoleNotifier.OUT_ERRORS).delete();
		new File(ConsoleNotifier.OUT_TABLES).delete();
		return new ConsoleNotifier(options.getDebug());
	}

	@Override
	protected IBundleLoader createTemplateLoader(String path) {
		File folder = new File(path);
		if (folder.isDirectory()) {
			return new FolderTemplateLoader(new File[] { folder }, "utf8"); // FIXME charset
		}
		return null;
	}

	private static class ConsoleNotifier implements INotifier {

		static final String OUT_ERRORS = "errors";
		static final String OUT_TABLES = "tables";

		private PrintStream debug, warn;
		private int debuglev;

		public ConsoleNotifier(int debuglev) {
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
	}
}
