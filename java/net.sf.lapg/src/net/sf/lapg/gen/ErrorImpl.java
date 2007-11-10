package net.sf.lapg.gen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import net.sf.lapg.IError;

public class ErrorImpl implements IError {

	static final String OUT_ERRORS = "errors";
	static final String OUT_TABLES = "tables";

	PrintStream debug, warn;
	int debuglev;

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
}
