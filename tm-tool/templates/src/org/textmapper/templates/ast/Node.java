/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.templates.ast;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.ast.TemplatesTree.TextSource;

public abstract class Node implements SourceElement {

	private final TextSource source;
	private final int offset, endoffset;

	protected Node(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	protected abstract void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env);

	public abstract void toJavascript(StringBuilder sb);

	public String getLocation() {
		return source.getLocation(offset);
	}

	@Override
	public int getLine() {
		return source.lineForOffset(offset);
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getEndOffset() {
		return endoffset;
	}

	public TextSource getInput() {
		return source;
	}

	@Override
	public String getResourceName() {
		return source.getFile();
	}

	@Override
	public String toString() {
		return source.getText(offset, endoffset);
	}
}
