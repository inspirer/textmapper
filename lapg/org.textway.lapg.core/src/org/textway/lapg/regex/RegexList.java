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
package org.textway.lapg.regex;

import org.textway.lapg.api.regex.RegexContext;
import org.textway.lapg.api.regex.RegexVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexList extends RegexPart implements org.textway.lapg.api.regex.RegexList {

	private List<RegexPart> elements = new ArrayList<RegexPart>();
	private boolean inParentheses;

	public RegexList(RegexPart initialPart) {
		super(initialPart.getInput(), initialPart.getOffset(), initialPart.getEndOffset());
		elements.add(initialPart);
		inParentheses = false;
	}

	public void addElement(RegexPart part) {
		if (inParentheses) {
			throw new IllegalStateException("cannot add elements");
		}
		elements.add(part);
		include(part);
	}

	public Collection<org.textway.lapg.api.regex.RegexPart> getElements() {
		return Collections.<org.textway.lapg.api.regex.RegexPart>unmodifiableCollection(elements);
	}

	public void setInParentheses() {
		this.inParentheses = true;
	}

	public boolean isInParentheses() {
		return inParentheses;
	}

	@Override
	public boolean isConstant() {
		for (RegexPart p : elements) {
			if (!p.isConstant()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getConstantValue() {
		StringBuilder sb = new StringBuilder();
		for (RegexPart p : elements) {
			String current = p.getConstantValue();
			if (current == null) {
				return null;
			}
			sb.append(current);
		}
		return sb.toString();
	}

	@Override
	protected void toString(StringBuilder sb) {
		if (inParentheses) {
			sb.append('(');
		}
		for (RegexPart p : elements) {
			p.toString(sb);
		}
		if (inParentheses) {
			sb.append(')');
		}
	}

	@Override
	public void accept(RegexVisitor visitor) {
		visitor.visitBefore(this);
		for (RegexPart element : elements) {
			element.accept(visitor);
		}
		visitor.visitAfter(this);
	}

	@Override
	public int getLength(RegexContext context) {
		int result = 0;
		for (RegexPart element : elements) {
			int len = element.getLength(context);
			if (len == -1) {
				return -1;
			}
			result += len;
		}
		return result;
	}
}
