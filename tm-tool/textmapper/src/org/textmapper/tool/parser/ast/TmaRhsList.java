/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

import java.util.List;

/**
 * evgeny, 8/7/12
 */
public class TmaRhsList extends TmaNode implements ITmaRhsPart {

	private final List<ITmaRhsPart> ruleParts;
	private final List<TmaSymref> separator;
	private final boolean atLeastOne;

	public TmaRhsList(List<ITmaRhsPart> ruleParts, List<TmaSymref> separator, boolean atLeastOne, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.ruleParts = ruleParts;
		this.separator = separator;
		this.atLeastOne = atLeastOne;
	}

	public List<ITmaRhsPart> getRuleParts() {
		return ruleParts;
	}

	public List<TmaSymref> getSeparator() {
		return separator;
	}

	public boolean isAtLeastOne() {
		return atLeastOne;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (ruleParts != null) {
			for (ITmaRhsPart rulePart : ruleParts) {
				rulePart.accept(v);
			}
		}
		if (separator != null) {
			for (TmaSymref ref : separator) {
				ref.accept(v);
			}
		}
	}
}
