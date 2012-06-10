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
class RegexOr extends RegexPart implements org.textway.lapg.api.regex.RegexOr {

	List<RegexPart> variants = new ArrayList<RegexPart>();

	public RegexOr(RegexPart initialPart) {
		super(initialPart.getInput(), initialPart.getOffset(), initialPart.getEndOffset());
		variants.add(initialPart);
	}

	public void addVariant(RegexPart part) {
		variants.add(part);
		include(part);
	}

	public Collection<org.textway.lapg.api.regex.RegexPart> getVariants() {
		return Collections.<org.textway.lapg.api.regex.RegexPart>unmodifiableCollection(variants);
	}

	@Override
	protected void toString(StringBuilder sb) {
		boolean first = true;
		for (RegexPart p : variants) {
			if (!first) {
				sb.append("|");
			} else {
				first = false;
			}
			p.toString(sb);
		}
	}

	@Override
	public void accept(RegexVisitor visitor) {
		visitor.visitBefore(this);
		boolean first = true;
		for (RegexPart element : variants) {
			if (!first) {
				visitor.visitBetween(this);
			} else {
				first = false;
			}
			element.accept(visitor);
		}
		visitor.visitAfter(this);
	}

	@Override
	public int getLength(RegexContext context) {
		int result = -1;
		for (RegexPart variant : variants) {
			int len = variant.getLength(context);
			if (len == -1 || result != -1 && len != result) {
				return -1;
			}
			result = len;
		}
		return result;
	}
}
