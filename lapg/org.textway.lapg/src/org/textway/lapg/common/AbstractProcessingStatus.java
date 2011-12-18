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
import org.textway.lapg.parser.TextSourceElement;
import org.textway.lapg.parser.ast.IAstNode;

import java.util.Map;

public abstract class AbstractProcessingStatus implements ProcessingStatus {

	private final boolean isDebug;
	private final boolean isAnalysis;
	private Map<SourceElement, IAstNode> sourceMap;

	protected AbstractProcessingStatus(boolean debug, boolean analysis) {
		isDebug = debug;
		isAnalysis = analysis;
	}

	public void setSourceMap(Map<SourceElement, IAstNode> sourceMap) {
		this.sourceMap = sourceMap;
	}

	@Override
	public void report(int kind, String message, SourceElement... anchors) {
		SourceElement anchor = anchors != null && anchors.length > 0 ? anchors[0] : null;
		if (anchor != null) {
			String location = getLocation(anchor);
			if (location != null) {
				message = location + message;
			}
		}
		switch (kind) {
			case KIND_FATAL:
			case KIND_ERROR:
				handle(KIND_ERROR, message + "\n");
				break;
			case KIND_WARN:
			case KIND_INFO:
				handle(kind, message + "\n");
				break;
		}
	}

	@Override
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

	@Override
	public boolean isDebugMode() {
		return isDebug;
	}

	@Override
	public boolean isAnalysisMode() {
		return isAnalysis;
	}

	@Override
	public void debug(String info) {
		handle(KIND_DEBUG, info);
	}

	public String getLocation(SourceElement element) {
		if (element instanceof TextSourceElement) {
			TextSourceElement textElement = (TextSourceElement) element;
			if (textElement.getResourceName() != null) {
				return textElement.getResourceName() + "," + textElement.getLine() + ": ";
			}
		} else if (sourceMap != null && sourceMap.containsKey(element)) {
			IAstNode node = sourceMap.get(element);
			if (node.getInput().getFile() != null) {
				return node.getInput().getFile() + "," + node.getLine() + ": ";
			}
		}
		return null;
	}

	public abstract void handle(int kind, String text);
}
