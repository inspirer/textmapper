/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.regex;

import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexList;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.regex.RegexSwitch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstList extends RegexAstPart implements RegexList {

	private List<RegexAstPart> elements = new ArrayList<>();
	private boolean inParentheses;

	public RegexAstList(RegexAstPart initialPart) {
		super(initialPart.getInput(), initialPart.getOffset(), initialPart.getEndOffset());
		elements.add(initialPart);
		inParentheses = false;
	}

	public void addElement(RegexAstPart part) {
		if (inParentheses) {
			throw new IllegalStateException("cannot add elements");
		}
		elements.add(part);
		include(part);
	}

	@Override
	public Collection<RegexPart> getElements() {
		return Collections.<RegexPart>unmodifiableCollection(elements);
	}

	public void setInParentheses() {
		this.inParentheses = true;
	}

	@Override
	public boolean isParenthesized() {
		return inParentheses;
	}

	@Override
	public boolean isConstant() {
		for (RegexAstPart p : elements) {
			if (!p.isConstant()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getConstantValue() {
		StringBuilder sb = new StringBuilder();
		for (RegexAstPart p : elements) {
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
		for (RegexAstPart p : elements) {
			p.toString(sb);
		}
		if (inParentheses) {
			sb.append(')');
		}
	}

	@Override
	public int getLength(RegexContext context) {
		int result = 0;
		for (RegexAstPart element : elements) {
			int len = element.getLength(context);
			if (len == -1) {
				return -1;
			}
			result += len;
		}
		return result;
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseList(this);
	}
}
