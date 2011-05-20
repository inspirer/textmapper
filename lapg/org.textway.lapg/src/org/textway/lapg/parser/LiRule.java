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
package org.textway.lapg.parser;

import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;
import org.textway.lapg.parser.ast.IAstNode;
import org.textway.templates.api.INamedEntity;

import java.util.Map;

public class LiRule extends LiAnnotated implements Rule, INamedEntity {

	private static final LiSymbolRef[] EMPTY_RIGHT = new LiSymbolRef[0];

	private int index;
	private final String alias;
	private final LiSymbol left;
	private final LiSymbolRef[] right;
	private final SourceElement code;
	private final LiSymbol priority;
	private final IAstNode node;

	public LiRule(String alias, LiSymbol left, LiSymbolRef[] right, SourceElement code, LiSymbol priority, Map<String, Object> annotations, IAstNode node) {
		super(annotations, node);
		this.left = left;
		this.right = right == null ? EMPTY_RIGHT : right;
		this.code = code;
		this.priority = priority;
		this.node = node;
		this.alias = alias;
	}

	public SourceElement getAction() {
		return code;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getAlias() {
		return alias;
	}

	public Symbol getLeft() {
		return left;
	}

	public SymbolRef[] getRight() {
		return right;
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

	public IAstNode getNode() {
		return node;
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
		for (LiSymbolRef s : right) {
			sb.append(" ");
			if (s.getTarget().getName() == null) {
				sb.append("{}");
			} else {
				sb.append(s.getTarget().getName());
			}
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
