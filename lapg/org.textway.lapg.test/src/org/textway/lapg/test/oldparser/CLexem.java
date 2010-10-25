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
package org.textway.lapg.test.oldparser;

import org.textway.lapg.api.Action;
import org.textway.lapg.api.Lexem;
import org.textway.templates.api.ILocatedEntity;

public class CLexem implements ILocatedEntity, Lexem {

	private final CSymbol sym;
	private final String regexp;
	private final CAction action;
	private final int priority;
	private final int groups;

	private final String input;
	private final int line;

	public CLexem(CSymbol sym, String regexp, CAction action, int priority, int groups, String input, int line) {
		this.sym = sym;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
		this.groups = groups;
		this.input = input;
		this.line = line;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public CSymbol getSymbol() {
		return sym;
	}

	public String getRegexp() {
		return regexp;
	}

	public Action getAction() {
		return action;
	}

	public int getPriority() {
		return priority;
	}

	public int getGroups() {
		return groups;
	}

	public int getEndOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getResourceName() {
		// TODO Auto-generated method stub
		return null;
	}
}
