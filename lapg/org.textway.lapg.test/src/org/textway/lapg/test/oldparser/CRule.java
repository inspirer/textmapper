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

import java.util.List;
import org.textway.lapg.api.Action;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.api.INamedEntity;

public class CRule implements ILocatedEntity, INamedEntity, Rule {

	private CSymbol left;
	private final SymbolRef[] right;
	private final CAction action;
	private final CSymbol priority;
	private final String input;
	private final int line;
	int index;

	public CRule(List<CSymbol> right, CAction action, CSymbol priority, String input, int line) {
		this.right = new SymbolRef[right != null ? right.size() : 0];
		if(right != null) {
			for(int i = 0; i < right.size(); i++) {
				this.right[i] = new CSymbolRef(right.get(i));
			}
		}
		this.action = action;
		this.priority = priority;
		this.index = -1;
		this.input = input;
		this.line = line;
	}

	void setLeft(CSymbol sym) {
		this.left = sym;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public CSymbol getLeft() {
		return left;
	}

	public SymbolRef[] getRight() {
		return right;
	}

	public Action getAction() {
		return action;
	}

	public void addAnnotation(String name, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object getAnnotation(String name) {
		return null;
	}

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

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (left.getName() == null) {
			sb.append("<noname>");
		} else {
			sb.append(left.getName());
		}
		sb.append(" ::=");
		for (SymbolRef s : right) {
			sb.append(" ");
			if (s.getTarget().getName() == null) {
				sb.append("{}");
			} else {
				sb.append(s.getTarget().getName());
			}
		}
		if (priority != null) {
			sb.append(" << ");
			sb.append(priority.getName());
		}
		return sb.toString();
	}

	public String getTitle() {
		return "Rule `" + toString() + "`";
	}

	private static class CSymbolRef implements SymbolRef, ILocatedEntity {

		CSymbol target;

		public CSymbolRef(CSymbol target) {
			this.target = target;
		}

		public String getAlias() {
			return null;
		}

		public Symbol getTarget() {
			return target;
		}

		public void addAnnotation(String name, Object value) {
			throw new UnsupportedOperationException();
		}

		public Object getAnnotation(String name) {
			return null;
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

		public String getLocation() {
			// TODO Auto-generated method stub
			return null;
		}
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
