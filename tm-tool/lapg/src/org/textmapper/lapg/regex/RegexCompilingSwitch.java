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
package org.textmapper.lapg.regex;

import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.regex.RegexQuantifier;
import org.textmapper.lapg.api.regex.RegexSwitch;

/**
 * evgeny, 7/13/12
 */
public abstract class RegexCompilingSwitch extends RegexSwitch<Void> {

	@Override
	public Void caseQuantifier(RegexQuantifier c) {
		int min = c.getMin();
		int max = c.getMax();
		if (min < 0 || max == 0 || max < -1 || max > 0 && min > max) {
			throw new IllegalArgumentException("wrong quantifier: " + c.toString());
		}

		while (min > 1) {
			c.getInner().accept(this);
			min--;
			if (max != -1) {
				max--;
			}
		}

		assert min == 0 || min == 1;

		while (max > 1) {
			yield(c.getInner(), true, false);
			max--;
		}

		assert max == -1 || max == 1;

		yield(c.getInner(), min == 0, max == -1);
		return null;
	}

	public abstract void yield(RegexPart part, boolean optional, boolean multiple);
}
