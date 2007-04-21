package net.sf.lapg.gen;

import java.io.PrintStream;

import net.sf.lapg.lalr.IError;

public class ErrorImpl implements IError {
	
	PrintStream error, debug, warn;
	
	public ErrorImpl(PrintStream error, PrintStream debug, PrintStream warn) {
		this.error = error;
		this.debug = debug;
		this.warn = warn;
	}

	public void debug(String info) {
		debug.print(info);
	}

	public void error(String error) {
		this.error.print(error);
	}

	public void warn(String warning) {
		warn.print(warning);
	}
}
