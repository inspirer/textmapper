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

public class LiLexem extends LiEntity implements Lexem {

	private final Symbol sym;
	private final String regexp;
	private final int groups;
	private final int priority;
	private final SourceElement action;

	public LiLexem(Symbol sym, String regexp, int groups, int priority, SourceElement action, IAstNode node) {
		super(node);
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
		return regexp;
	}

	public Symbol getSymbol() {
		return sym;
	}
}
