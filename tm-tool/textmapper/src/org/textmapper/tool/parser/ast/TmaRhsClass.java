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

public class TmaRhsClass extends TmaNode implements ITmaRhsPart {

	private TmaIdentifier identifier;
	private ITmaRhsPart rhsPrimary;

	public TmaRhsClass(TmaIdentifier identifier, ITmaRhsPart rhsPrimary, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.rhsPrimary = rhsPrimary;
	}

	public TmaIdentifier getIdentifier() {
		return identifier;
	}

	public ITmaRhsPart getInner() {
		return rhsPrimary;
	}

	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (identifier != null) {
			identifier.accept(v);
		}
		if (rhsPrimary != null) {
			rhsPrimary.accept(v);
		}
	}
}
