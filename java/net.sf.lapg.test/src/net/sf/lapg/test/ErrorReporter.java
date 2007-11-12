package net.sf.lapg.test;

import junit.framework.Assert;
import net.sf.lapg.IError;

public class ErrorReporter implements IError {

	public void debug(String info) {
		Assert.fail(info);
	}

	public void error(String error) {
		Assert.fail(error);
	}

	public void warn(String warning) {
		Assert.fail(warning);
	}

}
