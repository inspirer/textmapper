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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Lexem;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.regex.RegexPart;

class LiLexem implements Lexem, DerivedSourceElement {

	private final int kind;
	private final int index;
	private final Symbol sym;
	private final RegexPart regexp;
	private final int groups;
	private final int priority;
	private final Lexem classLexem;
	private final SourceElement origin;

	public LiLexem(int kind, int index, Symbol sym, RegexPart regexp, int groups, int priority, Lexem classLexem, SourceElement origin) {
		this.kind = kind;
		this.index = index;
		this.sym = sym;
		this.regexp = regexp;
		this.groups = groups;
		this.priority = priority;
		this.classLexem = classLexem;
		this.origin = origin;
	}

	@Override
	public int getGroups() {
		return groups;
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
	public Symbol getSymbol() {
		return sym;
	}

	@Override
	public Lexem getClassLexem() {
		return classLexem;
	}

	@Override
	public boolean isExcluded() {
		return classLexem != null;
	}

	public String getTitle() {
		return "Lexem `" + sym.getName() + "`";
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
