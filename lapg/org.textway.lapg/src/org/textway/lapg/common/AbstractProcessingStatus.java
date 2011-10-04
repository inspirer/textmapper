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
package org.textway.lapg.common;

import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;

public abstract class AbstractProcessingStatus implements ProcessingStatus {

	private final boolean isDebug;
	private final boolean isAnalysis;

	protected AbstractProcessingStatus(boolean debug, boolean analysis) {
		isDebug = debug;
		isAnalysis = analysis;
	}

	public void report(int kind, String message, SourceElement... anchors) {
		SourceElement anchor = anchors != null && anchors.length > 0 ? anchors[0] : null;
		switch (kind) {
			case KIND_FATAL:
			case KIND_ERROR:
				if (anchor != null && anchor.getResourceName() != null) {
					handle(KIND_ERROR, anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				handle(KIND_ERROR, message + "\n");
				break;
			case KIND_WARN:
				if (anchor != null && anchor.getResourceName() != null) {
					handle(KIND_WARN, anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				handle(KIND_WARN, message + "\n");
				break;
			case KIND_INFO:
				if (anchor != null && anchor.getResourceName() != null) {
					handle(KIND_INFO, anchor.getResourceName() + "," + anchor.getLine() + ": ");
				}
				handle(KIND_INFO, message + "\n");
				break;
		}
	}

	public void report(ParserConflict conflict) {
		Rule rule = conflict.getRules()[0];
		if (conflict.getKind() == ParserConflict.FIXED) {
			if (isAnalysisMode()) {
				report(KIND_WARN, conflict.getText(), rule);
			}
		} else {
			report(KIND_ERROR, conflict.getText(), rule);
		}
	}

	public boolean isDebugMode() {
		return isDebug;
	}

	public boolean isAnalysisMode() {
		return isAnalysis;
	}

	public void debug(String info) {
		handle(KIND_DEBUG, info);
	}

	public abstract void handle(int kind, String error);
}
