package net.sf.lapg.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;
import net.sf.lapg.INotifier;

public class TestNotifier implements INotifier {

	private final StringBuffer warns = new StringBuffer();
	private final StringBuffer errors = new StringBuffer();

	public TestNotifier() {
	}

	public TestNotifier(String warns, String errors) {
		this.warns.append(warns);
		this.errors.append(errors);
	}

	public void debug(String info) {
		Assert.fail(info);
	}

	@Override
	public void info(String info) {
		// ignore
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

	public void dispose() {
	}

	public void trace(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		error(sw.getBuffer().toString());
	}
}
