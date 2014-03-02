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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.regex.RegexPart;

class LiLexerRule extends LiUserDataHolder implements LexerRule, DerivedSourceElement {

	private final int kind;
	private final int index;
	private final Terminal sym;
	private final RegexPart regexp;
	private final Iterable<LexerState> states;
	private final int priority;
	private final LexerRule classLexerRule;
	private final SourceElement origin;

	public LiLexerRule(int kind, int index, Terminal sym, RegexPart regexp, Iterable<LexerState> states, int priority, LexerRule classLexerRule, SourceElement origin) {
		this.kind = kind;
		this.index = index;
		this.sym = sym;
		this.regexp = regexp;
		this.states = states;
		this.priority = priority;
		this.classLexerRule = classLexerRule;
		this.origin = origin;
	}

	@Override
	public Iterable<LexerState> getStates() {
		return states;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public RegexPart getRegexp() {
		return regexp;
	}

	@Override
	public int getKind() {
		return kind;
	}

	@Override
	public String getKindAsText() {
		switch (kind) {
			case KIND_CLASS:
				return "class";
			case KIND_SOFT:
				return "soft";
			case KIND_SPACE:
				return "space";
		}
		return "none";
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Terminal getSymbol() {
		return sym;
	}

	@Override
	public LexerRule getClassRule() {
		return classLexerRule;
	}

	@Override
	public boolean isExcluded() {
		return classLexerRule != null;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
