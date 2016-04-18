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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaSetBinary extends TmaNode implements ITmaSetExpression {

	private final ITmaSetExpression left;
	private final TmaSetBinary.TmaKindKind kind;
	private final ITmaSetExpression right;

	public TmaSetBinary(ITmaSetExpression left, TmaSetBinary.TmaKindKind kind, ITmaSetExpression right, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.left = left;
		this.kind = kind;
		this.right = right;
	}

	public ITmaSetExpression getLeft() {
		return left;
	}

	public TmaSetBinary.TmaKindKind getKind() {
		return kind;
	}

	public ITmaSetExpression getRight() {
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

	public enum TmaKindKind {
		OR,
		AND,
	}
}
