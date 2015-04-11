/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.gen;

import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.TextSourceElement;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;

/**
 * Gryaznov Evgeny, 2/25/12
 */
public final class TemplatesStatusAdapter implements TemplatesStatus {

	private final ProcessingStatus status;

	public TemplatesStatusAdapter(ProcessingStatus status) {
		this.status = status;
	}

	@Override
	public void report(int kind, String message, SourceElement... anchors) {
		if (anchors != null) {
			TextSourceElement[] n = new TextSourceElement[anchors.length];
			for (int i = 0; i < n.length; i++) {
				n[i] = anchors[i] != null ? new TemplateSourceElementAdapter(anchors[i]) : null;
			}
			status.report(kind, message, n);
		} else {
			status.report(kind, message);
		}
	}

	private static final class TemplateSourceElementAdapter implements TextSourceElement {

		/**
		 * template node
		 */
		private final SourceElement myWrapped;

		public TemplateSourceElementAdapter(SourceElement element) {
			myWrapped = element;
		}

		public int getOffset() {
			return myWrapped.getOffset();
		}

		public int getEndoffset() {
			return myWrapped.getEndOffset();
		}

		public int getLine() {
			return myWrapped.getLine();
		}

		public String getText() {
			return myWrapped.toString();
		}

		public String getResourceName() {
			return myWrapped.getResourceName();
		}
	}
}
