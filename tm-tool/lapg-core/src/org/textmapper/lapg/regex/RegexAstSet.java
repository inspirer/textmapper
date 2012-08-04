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

import org.textmapper.lapg.api.regex.*;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstSet extends RegexAstPart implements RegexSet {

	private final CharacterSet set;
	private final List<RegexAstPart> charset;

	public RegexAstSet(CharacterSet set, List<RegexAstPart> charset, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.set = set;
		this.charset = charset;
	}

	public CharacterSet getSet() {
		return set;
	}

	public Collection<RegexPart> getCharset() {
		return Collections.<RegexPart>unmodifiableCollection(charset);
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append('[');
		if (set.isInverted()) {
			sb.append('^');
		}
		for (RegexAstPart p : charset) {
			if (p instanceof RegexAstChar) {
				RegexUtil.escape(sb, ((RegexAstChar) p).getChar(), true);
			} else {
				p.toString(sb);
			}
		}
		sb.append(']');
	}

	@Override
	public int getLength(RegexContext context) {
		return 1;
	}

	@Override
	public <T> T accept(RegexSwitch<T> switch_) {
		return switch_.caseSet(this);
	}
}
