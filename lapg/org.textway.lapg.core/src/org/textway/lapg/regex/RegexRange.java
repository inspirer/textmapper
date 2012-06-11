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
import org.textway.lapg.api.regex.RegexSwitch;
import org.textway.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexRange extends RegexPart implements org.textway.lapg.api.regex.RegexRange {

	private char left;
	private char right;

	public RegexRange(char left, char right, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.left = left;
		this.right = right;
	}

	public char getLeft() {
		return left;
	}

	public char getRight() {
		return right;
	}

	@Override
	protected void toString(StringBuilder sb) {
		RegexUtil.escape(sb, left, true);
		sb.append('-');
		RegexUtil.escape(sb, right, true);
	}

	@Override
	public int getLength(RegexContext context) {
		return 1;
	}

	@Override
	public void accept(RegexSwitch switch_) {
		switch_.caseRange(this);
	}
}
