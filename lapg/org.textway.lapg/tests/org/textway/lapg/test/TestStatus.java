/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.test;

import org.textway.lapg.gen.LapgOptions;

import org.textway.lapg.common.AbstractProcessingStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;

public class TestStatus extends AbstractProcessingStatus {

	private final StringBuilder warns = new StringBuilder();
	private final StringBuilder errors = new StringBuilder();

	public TestStatus() {
		super(false, false);
	}

	public TestStatus(String warns, String errors) {
		this(warns, errors, 0);
	}

	public TestStatus(String warns, String errors, int debuglev) {
		super(debuglev >= LapgOptions.DEBUG_TABLES, debuglev >= LapgOptions.DEBUG_AMBIG);
		this.warns.append(warns);
		this.errors.append(errors);
	}

	private void error(String error) {
		if (errors.toString().startsWith(error)) {
			errors.replace(0, error.length(), "");
		} else {
			Assert.assertEquals(errors.toString(), error);
		}
	}

	private void warn(String warning) {
		if (warns.toString().startsWith(warning)) {
			warns.replace(0, warning.length(), "");
		} else {
			Assert.assertEquals(warns.toString(), warning);
		}
	}

	public void assertDone() {
		Assert.assertEquals(warns.toString(), "");
		Assert.assertEquals(errors.toString(), "");
	}

	public void reset(String warns, String errors) {
		this.warns.append(warns);
		this.errors.append(errors);
	}

	public void dispose() {
	}

	public void trace(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		error(sw.getBuffer().toString());
	}

	@Override
	public void report(String message, Throwable th) {
		error(message + "\n");
		if (th != null) {
			trace(th);
		}
	}

	@Override
	public void handle(int kind, String text) {
		if(kind == KIND_DEBUG) {
			Assert.fail(text);
		} else if(kind == KIND_ERROR || kind == KIND_FATAL) {
			error(text);
		} else if(kind == KIND_WARN) {
			warn(text);
		}
	}
}
