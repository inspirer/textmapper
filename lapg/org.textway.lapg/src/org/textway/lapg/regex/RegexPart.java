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

import org.textway.lapg.api.regex.RegexVisitor;
import org.textway.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public abstract class RegexPart implements org.textway.lapg.api.regex.RegexPart {

	private final TextSource source;
	private int offset, endoffset;

	public RegexPart(TextSource source, int offset, int endoffset) {
		this.source = source;
		this.offset = offset;
		this.endoffset = endoffset;
	}

	public int getOffset() {
		return offset;
	}

	public int getEndOffset() {
		return endoffset;
	}

	public TextSource getInput() {
		return source;
	}

	protected void include(RegexPart part) {
		if (part.offset < this.offset) {
			this.offset = part.offset;
		}
		if (part.endoffset > this.endoffset) {
			this.endoffset = part.endoffset;
		}
	}

	public boolean isConstant() {
		return false;
	}

	public String getConstantValue() {
		return null;
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	protected abstract void toString(StringBuilder sb);

	public abstract void accept(RegexVisitor visitor);
}
