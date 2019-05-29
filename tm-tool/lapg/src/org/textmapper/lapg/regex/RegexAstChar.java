/**
 * Copyright 2002-2019 Evgeny Gryaznov
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

import org.textmapper.lapg.api.regex.RegexChar;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexSwitch;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstChar extends RegexAstPart implements RegexChar {

	private final int c;

	public RegexAstChar(int c, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.c = c;
	}

	@Override
	public int getChar() {
		return c;
	}

	@Override
	public boolean isConstant() {
		return c != -1;
	}

	@Override
	public String getConstantValue() {
 		return new String(Character.toChars(c));
	}

	@Override
	protected void toString(StringBuilder sb) {
		if (c == -1) {
			sb.append("{eoi}");
		} else {
			RegexUtil.escape(sb, c, false);
		}
	}

	@Override
	public int getLength(RegexContext context) {
		return 1;
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseChar(this);
	}
}
