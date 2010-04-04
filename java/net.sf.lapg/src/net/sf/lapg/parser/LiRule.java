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
package net.sf.lapg.parser;

import java.util.Map;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;
import net.sf.lapg.parser.ast.AstNode;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INamedEntity;

public class LiRule extends LiAnnotated implements Rule, ILocatedEntity, INamedEntity {

	private static final LiSymbolRef[] EMPTY_RIGHT = new LiSymbolRef[0];

	private int index;
	private final LiSymbol left;
	private final LiSymbolRef[] right;
	private final Action code;
	private final LiSymbol priority;
	private final AstNode node;

	public LiRule(LiSymbol left, LiSymbolRef[] right, Action code, LiSymbol priority, AstNode node, Map<String,Object> annotations) {
		super(annotations);
		this.left = left;
		this.right = right == null ? EMPTY_RIGHT : right ;
		this.code = code;
		this.priority = priority;
		this.node = node;
	}

	public Action getAction() {
		return code;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public AstNode getNode() {
		return node;
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
		for (LiSymbolRef s : right) {
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

	public String getLocation() {
		return node.getLocation();
	}
}
