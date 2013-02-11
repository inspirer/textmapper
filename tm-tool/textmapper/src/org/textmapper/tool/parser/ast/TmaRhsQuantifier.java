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
 * evgeny, 8/3/12
 */
public class TmaRhsQuantifier extends AstNode implements TmaRhsPart {

	public static final int KIND_OPTIONAL = 0;
	public static final int KIND_ZEROORMORE = 1;
	public static final int KIND_ONEORMORE = 2;

	private final int quantifier;
	private final TmaRhsPart inner;

	public TmaRhsQuantifier(TmaRhsPart inner, int quantifier, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.inner = inner;
		this.quantifier = quantifier;
	}

	public int getQuantifier() {
		return quantifier;
	}

	public TmaRhsPart getInner() {
		return inner;
	}

	public boolean isOptional() {
		return quantifier == KIND_OPTIONAL;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (inner != null) {
			inner.accept(v);
		}
	}
}
