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
package org.textway.lapg.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;

public class TestStatus implements ProcessingStatus {

	private final StringBuffer warns = new StringBuffer();
	private final StringBuffer errors = new StringBuffer();
	private int debuglev;

	public TestStatus() {
	}

	public TestStatus(String warns, String errors) {
		this(warns,errors,0);
	}

	public TestStatus(String warns, String errors, int debuglev) {
		this.warns.append(warns);
		this.errors.append(errors);
		this.debuglev = debuglev;
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
			Assert.assertEquals(errors.toString(), error);
		}
	}

	public void warn(String warning) {
		if( warns.toString().startsWith(warning) ) {
			warns.replace(0, warning.length(), "");
		} else {
			Assert.assertEquals(warns.toString(), warning);
		}
	}

	public void assertDone() {
		Assert.assertEquals(warns.toString(), "");
		Assert.assertEquals(errors.toString(), "");
	}

	public void dispose() {
	}

	public void trace(Throwable ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		error(sw.getBuffer().toString());
	}

	public boolean isDebugMode() {
		return debuglev >= 2;
	}

	public boolean isAnalysisMode() {
		return debuglev >= 1;
	}

	public void report(String message, Throwable th) {
		error(message + "\n");
		if(th != null) {
			trace(th);
		}
	}

	public void report(int kind, String message, SourceElement ...anchors) {
		SourceElement anchor = anchors != null && anchors.length > 0 ? anchors[0] : null;
		switch(kind) {
		case KIND_FATAL:
		case KIND_ERROR:
			if(anchor != null && anchor.getResourceName() != null) {
				message = anchor.getResourceName() + "," + anchor.getLine() + ": " + message;
			}
			error(message + "\n");
			break;
		case KIND_WARN:
			if(anchor != null && anchor.getResourceName() != null) {
				message = anchor.getResourceName() + "," + anchor.getLine() + ": " + message;
			}
			warn(message + "\n");
			break;
		case KIND_INFO:
			if(anchor != null && anchor.getResourceName() != null) {
				message = anchor.getResourceName() + "," + anchor.getLine() + ": " + message;
			}
			info(message + "\n");
			break;
		}
	}

	public void report(ParserConflict conflict) {
		Rule rule = conflict.getRules()[0];
		if(conflict.getKind() == ParserConflict.FIXED) {
			if(isAnalysisMode()) {
				report(KIND_WARN, conflict.getText(), rule);
			}
		} else {
			report(KIND_ERROR, conflict.getText(), rule);
		}
	}
}
