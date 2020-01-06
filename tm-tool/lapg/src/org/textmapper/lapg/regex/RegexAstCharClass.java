/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexAstCharClass extends RegexAstSet {

	private final String cl;

	public RegexAstCharClass(String c, CharacterSet set, TextSource source, int offset, int endoffset) {
		super(set, null, source, offset, endoffset);
		this.cl = c;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append('\\');
		sb.append(cl.length() == 1 ? cl : "p{" + cl + "}");
	}
}
