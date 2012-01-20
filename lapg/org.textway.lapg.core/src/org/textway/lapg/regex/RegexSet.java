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

import org.textway.lapg.api.regex.CharacterSet;
import org.textway.lapg.api.regex.RegexVisitor;
import org.textway.lapg.regex.RegexDefTree.TextSource;

import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexSet extends RegexPart implements org.textway.lapg.api.regex.RegexSet {

	private final CharacterSet set;
	private final List<RegexPart> charset;

	public RegexSet(CharacterSet set, List<RegexPart> charset, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.set = set;
		this.charset = charset;
	}

	public CharacterSet getSet() {
		return set;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append('[');
		if (set.isInverted()) {
			sb.append('^');
		}
		for (RegexPart p : charset) {
			if (p instanceof RegexChar) {
				RegexUtil.escape(sb, ((RegexChar) p).getChar(), true);
			} else {
				p.toString(sb);
			}
		}
		sb.append(']');
	}

	@Override
	public void accept(RegexVisitor visitor) {
		if (!visitor.visit(this)) {
			return;
		}

		if (charset != null) {
			for (RegexPart part : charset) {
				part.accept(visitor);
			}
		}
	}
}
