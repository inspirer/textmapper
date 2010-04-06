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
package net.sf.lapg.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;
import net.sf.lapg.gen.INotifier;

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
