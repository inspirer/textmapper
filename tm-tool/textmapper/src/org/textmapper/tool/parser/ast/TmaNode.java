/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.TMTree.TextSource;

public abstract class TmaNode implements ITmaNode {

	protected TextSource source;
	protected int line;
	protected int offset;
	protected int endoffset;

	@Deprecated
	public TmaNode(TextSource source, int offset, int endoffset) {
		this(source, 0, offset, endoffset);
	}

	public TmaNode(TextSource source, int line, int offset, int endoffset) {
		this.source = source;
		this.line = line;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	public String getLocation() {
		return source.getLocation(offset);
	}

	public int getLine() {
		return this.line == 0 ? source.lineForOffset(offset) : this.line;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getEndoffset() {
		return this.endoffset;
	}

	public TextSource getSource() {
		return source;
	}

	public String getResourceName() {
		return source.getFile();
	}

	public String getText() {
		return source.getText(offset, endoffset);
	}

	@Override
	public String toString() {
		return source.getText(offset, endoffset);
	}
}
