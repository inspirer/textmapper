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
package org.textmapper.tool.bootstrap.b.ast;

import org.textmapper.tool.bootstrap.b.SampleBTree.TextSource;

public abstract class AstNode implements IAstNode {

	protected TextSource source;
	protected int offset;
	protected int endoffset;

	public AstNode(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	@Override
	public String getLocation() {
		return source.getLocation(offset);
	}

	@Override
	public int getOffset() {
		return this.offset;
	}

	@Override
	public int getEndoffset() {
		return this.endoffset;
	}

	@Override
	public TextSource getSource() {
		return source;
	}

	@Override
	public String getResourceName() {
		return source.getFile();
	}

	@Override
	public String getText() {
		return source.getText(offset, endoffset);
	}

	@Override
	public String toString() {
		return source == null ? "" : source.getText(offset, endoffset);
	}
}
