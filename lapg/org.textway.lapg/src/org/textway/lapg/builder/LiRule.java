/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.builder;

import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;

class LiRule implements Rule {

	private final int index;
	private final String alias;
	private final Symbol left;
	private final SymbolRef[] right;
	private final Symbol priority;

	public LiRule(int index, String alias, Symbol left, SymbolRef[] right, Symbol priority) {
		this.index = index;
		this.left = left;
		this.right = right;
		this.priority = priority;
		this.alias = alias;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Symbol getLeft() {
		return left;
	}

	@Override
	public SymbolRef[] getRight() {
		return right;
	}

	@Override
	public int getPriority() {
		if (priority != null) {
			return priority.getIndex();
		}
		for (int i = right.length - 1; i >= 0; i--) {
			if (right[i].getTarget().isTerm()) {
				return right[i].getTarget().getIndex();
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (left.getName() == null) {
			sb.append("<noname>");
		} else {
			sb.append(left.getName());
		}
		sb.append(" ::=");
		for (SymbolRef s : right) {
			sb.append(" ");
			sb.append(s.getTarget().getName());
		}
		if (priority != null) {
			sb.append(" %prio ");
			sb.append(priority.getName());
		}
		return sb.toString();
	}

	public String getTitle() {
		return "Rule `" + toString() + "`";
	}
}
