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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsRoot;
import org.textmapper.lapg.api.rule.RhsSequence;

/**
 * evgeny, 2/4/13
 */
abstract class LiRhsRoot extends LiRhsPart implements RhsRoot {

	private Nonterminal left;

	protected LiRhsRoot(Nonterminal left, SourceElement origin) {
		super(origin);
		this.left = left;
	}

	@Override
	public final Nonterminal getLeft() {
		return left;
	}

	void setLeft(Nonterminal left) {
		this.left = left;
	}

	protected void rewrite(RhsRoot part) {
		((LiNonterminal) left).rewriteDefinition(this, part);
	}

	@Override
	protected void setParent(LiRhsPart parent, boolean force) {
		throw new IllegalStateException("root element cannot be nested");
	}

	/**
	 * Pre-processing may split complex rules (like lists) into several intermediate rules,
	 * which become sources of the grammar rules.
	 */
	protected abstract RhsSequence[] preprocess();

	@Override
	public String getProvisionalName() {
		throw new UnsupportedOperationException();
	}
}
