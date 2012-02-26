/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.api.SourceElement;
import org.textway.templates.ast.TemplatesTree.TextSource;

public abstract class Node implements SourceElement {

	private final TextSource source;
	private final int offset, endoffset;

	protected Node(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	protected abstract void emit( StringBuilder sb, EvaluationContext context, IEvaluationStrategy env);

	public String getLocation() {
		return source.getLocation(offset);
	}

	public int getLine() {
		return source.lineForOffset(offset);
	}

	public int getOffset() {
		return offset;
	}

	public int getEndOffset() {
		return endoffset;
	}

	public TextSource getInput() {
		return source;
	}

	public String getResourceName() {
		return source.getFile();
	}

	@Override
	public String toString() {
		return source.getText(offset, endoffset);
	}
}
