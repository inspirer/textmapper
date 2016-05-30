/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

public class TmaDirectiveBrackets extends TmaNode implements ITmaLexerPart {

	private final TmaSymref opening;
	private final TmaSymref closing;

	public TmaDirectiveBrackets(TmaSymref opening, TmaSymref closing, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.opening = opening;
		this.closing = closing;
	}

	public TmaSymref getOpening() {
		return opening;
	}

	public TmaSymref getClosing() {
		return closing;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (opening != null) {
			opening.accept(v);
		}
		if (closing != null) {
			closing.accept(v);
		}
	}
}
