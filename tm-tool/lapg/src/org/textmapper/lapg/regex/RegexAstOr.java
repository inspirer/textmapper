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
import org.textmapper.lapg.api.regex.RegexOr;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.regex.RegexSwitch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstOr extends RegexAstPart implements RegexOr {

	List<RegexAstPart> variants = new ArrayList<>();

	public RegexAstOr(RegexAstPart initialPart) {
		super(initialPart.getInput(), initialPart.getOffset(), initialPart.getEndOffset());
		variants.add(initialPart);
	}

	public void addVariant(RegexAstPart part) {
		variants.add(part);
		include(part);
	}

	@Override
	public Collection<RegexPart> getVariants() {
		return Collections.<RegexPart>unmodifiableCollection(variants);
	}

	@Override
	protected void toString(StringBuilder sb) {
		boolean first = true;
		for (RegexAstPart p : variants) {
			if (!first) {
				sb.append("|");
			} else {
				first = false;
			}
			p.toString(sb);
		}
	}

	@Override
	public int getLength(RegexContext context) {
		int result = -1;
		for (RegexAstPart variant : variants) {
			int len = variant.getLength(context);
			if (len == -1 || result != -1 && len != result) {
				return -1;
			}
			result = len;
		}
		return result;
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseOr(this);
	}
}
