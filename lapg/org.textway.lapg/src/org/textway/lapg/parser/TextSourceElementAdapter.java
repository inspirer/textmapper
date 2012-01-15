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
package org.textway.lapg.parser;

import org.textway.lapg.parser.ast.IAstNode;

public class TextSourceElementAdapter implements TextSourceElement {

	private final IAstNode node;

	public TextSourceElementAdapter(IAstNode node) {
		this.node = node;
	}

	@Override
	public String getResourceName() {
		return node.getInput().getFile();
	}

	@Override
	public int getOffset() {
		return node.getOffset();
	}

	@Override
	public int getEndOffset() {
		return node.getEndOffset();
	}

	@Override
	public int getLine() {
		return node.getLine();
	}

	@Override
	public String getText() {
		return node.getInput().getText(node.getOffset(), node.getEndOffset());
	}

	@Override
	public String toString() {
		return getText();
	}
}
