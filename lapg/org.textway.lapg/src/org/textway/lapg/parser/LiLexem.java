/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.parser;

import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.parser.ast.IAstNode;
import org.textway.lapg.regex.RegexPart;
import org.textway.templates.api.INamedEntity;

public class LiLexem extends LiEntity implements Lexem, INamedEntity {

	private int kind;
	private final int index;
	private final Symbol sym;
	private final RegexPart regexp;
	private final int groups;
	private final int priority;
	private final SourceElement action;
	private LiLexem classLexem;

	public LiLexem(int kind, int index, Symbol sym, RegexPart regexp, int groups, int priority, SourceElement action, IAstNode node) {
		super(node);
		this.kind = kind;
		this.index = index;
		this.sym = sym;
		this.regexp = regexp;
		this.groups = groups;
		this.priority = priority;
		this.action = action;
	}

	public SourceElement getAction() {
		return action;
	}

	public int getGroups() {
		return groups;
	}

	public int getPriority() {
		return priority;
	}

	public String getRegexp() {
		return regexp.toString();
	}

	public RegexPart getParsedRegexp() {
		return regexp;
	}

	public int getKind() {
		return kind;
	}

	public String getKindAsText() {
		switch (kind) {
			case KIND_CLASS:
				return "class";
			case KIND_INSTANCE:
				return "instance";
			case KIND_SOFT:
				return "soft";
		}
		return "none";
	}

	public int getIndex() {
		return index;
	}

	public Symbol getSymbol() {
		return sym;
	}

	public Lexem getClassLexem() {
		return classLexem;
	}

	public boolean isExcluded() {
		return this.classLexem != null;
	}

	public String getTitle() {
		return "Lexem `" + sym.getName() + "`";
	}

	public void setClassLexem(LiLexem classLexem, boolean isSoft) {
		if(this.kind == KIND_CLASS) throw new IllegalStateException();

		this.classLexem = classLexem;
		this.kind = isSoft ? Lexem.KIND_SOFT : Lexem.KIND_INSTANCE;
	}
}
