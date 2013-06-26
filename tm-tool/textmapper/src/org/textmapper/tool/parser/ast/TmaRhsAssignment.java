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
 * evgeny, 2/10/13
 */
public class TmaRhsAssignment extends TmaNode implements TmaRhsPart {

	private final TmaIdentifier id;
	private final TmaRhsPart inner;
	private final boolean addition;

	public TmaRhsAssignment(TmaIdentifier id, TmaRhsPart inner, boolean isAddition, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.id = id;
		this.inner = inner;
		this.addition = isAddition;
	}

	public TmaIdentifier getId() {
		return id;
	}

	public TmaRhsPart getInner() {
		return inner;
	}

	public boolean isAddition() {
		return addition;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		id.accept(v);
		inner.accept(v);
	}
}
