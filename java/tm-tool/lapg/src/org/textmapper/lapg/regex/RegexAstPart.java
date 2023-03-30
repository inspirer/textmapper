/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
import org.textmapper.lapg.api.regex.RegexSwitch;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

/**
 * Gryaznov Evgeny, 4/5/11
 */
abstract class RegexAstPart implements RegexPart {

	private final TextSource source;
	private int offset, endoffset;

	public RegexAstPart(TextSource source, int offset, int endoffset) {
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

	@Override
	public TextSource getSource() {
		return source;
	}

	@Override
	public String getText() {
		return source.getText(offset, endoffset);
	}

	protected void include(int offset, int endoffset) {
		if (offset < this.offset) {
			this.offset = offset;
		}
		if (endoffset > this.endoffset) {
			this.endoffset = endoffset;
		}
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
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

	@Override
	public abstract <T> T accept(RegexSwitch<T> switch_);
}
