package net.sf.lapg.test;

import junit.framework.Assert;
import net.sf.lapg.IError;

public class ErrorReporter implements IError {

	public final StringBuffer warns = new StringBuffer();
	public final StringBuffer errors = new StringBuffer();

	public ErrorReporter() {
	}

	public void debug(String info) {
		Assert.fail(info);
	}

	public void error(String error) {
		if( errors.toString().startsWith(error) ) {
			errors.replace(0, error.length(), "");
		} else {
			Assert.fail(error);
		}
	}

	public void warn(String warning) {
		if( warns.toString().startsWith(warning) ) {
			warns.replace(0, warning.length(), "");
		} else {
			Assert.fail(warning);
		}
	}
}
