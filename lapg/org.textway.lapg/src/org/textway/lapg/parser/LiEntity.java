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
package org.textway.lapg.parser;

import org.textway.lapg.api.SourceElement;
import org.textway.lapg.parser.ast.IAstNode;
import org.textway.templates.api.ILocatedEntity;

public class LiEntity implements SourceElement, ILocatedEntity {

	private final IAstNode node;

	public LiEntity(IAstNode node) {
		this.node = node;
	}

	public int getOffset() {
		return node == null ? 0 : node.getOffset();
	}

	public int getEndOffset() {
		return node == null ? 0 : node.getEndOffset();
	}

	public int getLine() {
		return node == null ? 1 : node.getLine();
	}

	public String getResourceName() {
		return node == null ? null : node.getInput().getFile();
	}

	public String getLocation() {
		return node == null ? "<unknown>" : getResourceName() + "," + getLine();
	}
}
