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

public class TmaOption extends TmaNode {

	private final String key;
	private final ITmaExpression value;
	private final TmaSyntaxProblem syntaxProblem;

	public TmaOption(String key, ITmaExpression value, TmaSyntaxProblem syntaxProblem, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.key = key;
		this.value = value;
		this.syntaxProblem = syntaxProblem;
	}

	public String getKey() {
		return key;
	}

	public ITmaExpression getValue() {
		return value;
	}

	public TmaSyntaxProblem getSyntaxProblem() {
		return syntaxProblem;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (value != null) {
			value.accept(v);
		}
		if (syntaxProblem != null) {
			syntaxProblem.accept(v);
		}
	}
}
