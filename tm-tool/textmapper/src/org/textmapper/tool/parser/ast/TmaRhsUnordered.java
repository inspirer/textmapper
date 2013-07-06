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

/**
 * evgeny, 8/6/12
 */
public class TmaRhsUnordered extends TmaNode implements TmaRhsPart {

	private final TmaRhsPart left;
	private final TmaRhsPart right;

	public TmaRhsUnordered(TmaRhsPart left, TmaRhsPart right, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.left = left;
		this.right = right;
	}

	public TmaRhsPart getLeft() {
		return left;
	}

	public TmaRhsPart getRight() {
		return right;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (left != null) {
			left.accept(v);
		}
		if (right != null) {
			right.accept(v);
		}
	}
}
