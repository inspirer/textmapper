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
package org.textway.templates.types.ast;

import org.textway.templates.types.TypesTree.TextSource;

public class AstMultiplicity extends AstNode {

	private Integer lo;
	private boolean hasNoUpperBound;
	private Integer hi;

	public AstMultiplicity(Integer lo, boolean hasNoUpperBound, Integer hi, TextSource input, int start, int end) {
		super(input, start, end);
		this.lo = lo;
		this.hasNoUpperBound = hasNoUpperBound;
		this.hi = hi;
	}

	public Integer getLo() {
		return lo;
	}
	public boolean getHasNoUpperBound() {
		return hasNoUpperBound;
	}
	public Integer getHi() {
		return hi;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for lo
		// TODO for hi
	}
}
