package net.sf.lapg.test;

import junit.framework.Assert;
import net.sf.lapg.IError;

public class ErrorReporter implements IError {

	private final StringBuffer warns = new StringBuffer();
	private final StringBuffer errors = new StringBuffer();

	public ErrorReporter() {
	}

	public ErrorReporter(String warns, String errors) {
		this.warns.append(warns);
		this.errors.append(errors);
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

	public void assertDone() {
		Assert.assertEquals("", warns.toString());
		Assert.assertEquals("", errors.toString());
	}
}
