/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.a.ast;

import org.textmapper.tool.bootstrap.a.SampleATree.TextSource;

public abstract class AstNode implements IAstNode {

	protected TextSource source;
	protected int line;
	protected int offset;
	protected int column;
	protected int endline;
	protected int endoffset;
	protected int endcolumn;

	public AstNode(TextSource source, int line, int offset, int column, int endline, int endoffset, int endcolumn) {
		this.source = source;
		this.line = line;
		this.offset = offset;
		this.column = column;
		this.endline = endline;
		this.endoffset = endoffset;
		this.endcolumn = endcolumn;
	}

	@Override
	public String getLocation() {
		return source.getLocation(offset);
	}

	@Override
	public int getLine() {
		return this.line;
	}

	@Override
	public int getOffset() {
		return this.offset;
	}

	@Override
	public int getColumn() {
		return this.column;
	}

	@Override
	public int getEndline() {
		return this.endline;
	}

	@Override
	public int getEndoffset() {
		return this.endoffset;
	}

	@Override
	public int getEndcolumn() {
		return this.endcolumn;
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
