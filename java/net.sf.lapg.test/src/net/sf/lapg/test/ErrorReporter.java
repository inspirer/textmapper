package net.sf.lapg.test;

import net.sf.lapg.IError;

public class ErrorReporter implements IError {

	public void debug(String info) {
		System.out.print(info);
	}

	public void error(String error) {
		System.out.print(error);
	}

	public void warn(String warning) {
		System.out.print(warning);
	}

}
