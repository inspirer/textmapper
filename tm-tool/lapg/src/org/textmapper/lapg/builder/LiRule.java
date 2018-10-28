/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

class LiRule extends LiUserDataHolder implements Rule, DerivedSourceElement {

	private final int index;
	private final Nonterminal left;
	private final RhsCFPart[] right;
	private final Terminal precedence;
	private final RhsSequence definition;

	public LiRule(int index, Nonterminal left, RhsCFPart[] right,
				  Terminal precedence, RhsSequence definition) {
		this.index = index;
		this.left = left;
		this.right = right;
		this.precedence = precedence;
		this.definition = definition;
		((LiNonterminal) left).addRule(this);
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Nonterminal getLeft() {
		return left;
	}

	@Override
	public RhsCFPart[] getRight() {
		return right;
	}

	@Override
	public RhsSequence getSource() {
		return definition;
	}

	@Override
	public Terminal getPrecedenceSymbol() {
		return precedence;
	}

	@Override
	public int getPrecedence() {
		if (precedence != null) {
			return precedence.getIndex();
		}
		for (int i = right.length - 1; i >= 0; i--) {
			Symbol target = right[i].getTarget();
			if (target == null) continue;
			if (target.isTerm()) {
				return target.getIndex();
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (left.getNameText() == null) {
			sb.append("<noname>");
		} else {
			sb.append(left.getNameText());
		}
		sb.append(" :");
		for (RhsCFPart s : right) {
			sb.append(" ");
			switch (s.getKind()) {
				case Symbol:
					sb.append(LiUtil.getSymbolName((RhsSymbol) s));
					break;
				case StateMarker:
					((LiRhsStateMarker)s).toString(sb);
					break;
			}
		}
		if (precedence != null) {
			sb.append(" %prec ");
			sb.append(precedence.getNameText());
		}
		return sb.toString();
	}

	@Override
	public SourceElement getOrigin() {
		return definition;
	}
}
