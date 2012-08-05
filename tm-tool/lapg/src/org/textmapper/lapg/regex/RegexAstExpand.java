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
package org.textmapper.lapg.regex;

import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexExpand;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.regex.RegexSwitch;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstExpand extends RegexAstPart implements RegexExpand {

	private final String name;

	public RegexAstExpand(TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = source.getText(offset + 1, endoffset - 1);
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append('{');
		sb.append(name);
		sb.append('}');
	}

	public String getName() {
		return name;
	}

	@Override
	public int getLength(RegexContext context) {
		RegexPart regexPart = context.resolvePattern(name);
		if (regexPart == null) {
			return -1;
		}
		return regexPart.getLength(context);
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseExpand(this);
	}
}
