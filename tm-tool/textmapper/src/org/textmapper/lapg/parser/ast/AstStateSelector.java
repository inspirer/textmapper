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
package org.textmapper.lapg.parser.ast;

import org.textmapper.lapg.parser.LapgTree.TextSource;

import java.util.List;

public class AstStateSelector extends AstNode implements AstLexerPart {

	private final List<AstLexerState> states;

	public AstStateSelector(List<AstLexerState> states, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.states = states;
	}

	public List<AstLexerState> getStates() {
		return states;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (states != null) {
			for (AstLexerState state : states) {
				state.accept(v);
			}
		}
	}
}
