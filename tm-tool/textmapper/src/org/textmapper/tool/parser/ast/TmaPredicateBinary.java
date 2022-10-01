/**
 * Copyright 2002-2022 Evgeny Gryaznov
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

public class TmaPredicateBinary extends TmaNode implements ITmaPredicateExpression {

	private final ITmaPredicateExpression left;
	private final TmaPredicateBinary.TmaKindKind kind;
	private final ITmaPredicateExpression right;

	public TmaPredicateBinary(ITmaPredicateExpression left, TmaPredicateBinary.TmaKindKind kind, ITmaPredicateExpression right, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.left = left;
		this.kind = kind;
		this.right = right;
	}

	public ITmaPredicateExpression getLeft() {
		return left;
	}

	public TmaPredicateBinary.TmaKindKind getKind() {
		return kind;
	}

	public ITmaPredicateExpression getRight() {
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
		AND_AND,
		OR_OR,
	}
}
