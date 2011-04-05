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
package org.textway.lapg.regex;

import org.textway.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexQuantifier extends RegexPart {

	private final int min, max;
	private RegexPart inner;

	public RegexQuantifier(RegexPart inner, int min, int max, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.min = min;
		this.max = max;
		this.inner = inner;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public RegexPart getInner() {
		return inner;
	}

	@Override
	protected void toString(StringBuilder sb) {
		inner.toString(sb);
		if(min == 0 && max == -1) {
			sb.append('*');
		} else if(min == 0 && max == 1) {
			sb.append('?');
		} else if(min == 1 && max == -1) {
			sb.append('+');
		} else {
			sb.append('{');
			sb.append(min);
			if(min != max) {
				sb.append(',');
				if(max != -1) {
					sb.append(max);
				}
			}
			sb.append('}');
		}
	}
}
