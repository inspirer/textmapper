/*************************************************************
 * Copyright (c) 2002-2008 Evgeny Gryaznov
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

import net.sf.lapg.IError;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.impl.FolderTemplateLoader;

public class ConsoleGenerator extends AbstractGenerator {

	public ConsoleGenerator(LapgOptions options) {
		super(options);
	}

	@Override
	public void createFile(String name, String contents) {
		File file = new File(name);
		try {
			OutputStream os = new FileOutputStream(file);
			os.write(contents.getBytes("utf8"));
			os.close();
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
	}

	@Override
	public InputStream openInput() {
		InputStream stream;
		if( options.getInput() != null && !options.getInput().startsWith("-") ) {
			try {
				stream = new FileInputStream( options.getInput() );
			} catch( FileNotFoundException ex) {
				System.err.println( "lapg: file not found: " + options.getInput());
				return null;
			}
		} else {
			stream = System.in;
		}
		return stream;
	}

	@Override
	protected IError openError() {
		new File(ErrorImpl.OUT_ERRORS).delete();
		new File(ErrorImpl.OUT_TABLES).delete();
		return new ErrorImpl(options.getDebug());
	}

	@Override
	protected ITemplateLoader createTemplateLoader(String path) {
		File folder = new File(path);
		if(folder.isDirectory()) {
			return new FolderTemplateLoader(folder);
		}
		return null;
	}

	private static class ErrorImpl implements IError {

		static final String OUT_ERRORS = "errors";
		static final String OUT_TABLES = "tables";

		private PrintStream debug, warn;
		private int debuglev;

		public ErrorImpl(int debuglev) {
			this.debuglev = debuglev;
			this.debug = null;
			this.warn = null;
		}

		private PrintStream openFile(String name) {
			try {
				return new PrintStream(new FileOutputStream(name));
			} catch(FileNotFoundException ex) {
				error("lapg: IO error: " + ex.getMessage());
				return System.err;
			}
		}

		public void error(String error) {
			System.err.print(error);
		}

		public void debug(String info) {
			if( debuglev < 2 ) {
				return;
			}
			if( debug == null ) {
				debug = openFile(OUT_TABLES);
			}
			debug.print(info);
		}

		public void warn(String warning) {
			if( debuglev < 1 ) {
				return;
			}
			if( warn == null ) {
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
