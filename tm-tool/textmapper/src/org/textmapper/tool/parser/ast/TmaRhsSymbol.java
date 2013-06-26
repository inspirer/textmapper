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
 * evgeny, 8/2/12
 */
public class TmaRhsSymbol extends TmaNode implements TmaRhsPart {

	private final TmaReference reference;

	public TmaRhsSymbol(TmaReference reference, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.reference = reference;
	}

	public TmaReference getReference() {
		return reference;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (reference != null) {
			reference.accept(v);
		}
	}
}
