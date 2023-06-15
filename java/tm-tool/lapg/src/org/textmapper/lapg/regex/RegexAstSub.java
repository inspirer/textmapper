/**
 * Copyright 2002-2023 Evgeny Gryaznov
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
import org.textmapper.lapg.api.regex.RegexSet;
import org.textmapper.lapg.api.regex.RegexSub;
import org.textmapper.lapg.api.regex.RegexSwitch;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

public class RegexAstSub extends RegexAstPart implements RegexSub {

	private final RegexAstSet inner;  // never inverted

	public RegexAstSub(RegexAstSet inner, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.inner = inner;
	}

	@Override
	public RegexSet getInner() {
		return inner;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("-");
		inner.toString(sb);
	}

	@Override
	public int getLength(RegexContext context) {
		return 1;
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseSub(this);
	}
}
