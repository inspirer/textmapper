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
package net.sf.lapg.test.oldparser;

import net.sf.lapg.api.Action;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CAction implements Action, ILocatedEntity {

	private final String contents;

	private final String input;
	private final int line;

	public CAction(String contents, String input, int line) {
		this.contents = contents;
		this.input = input;
		this.line = line;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public String getContents() {
		return contents;
	}

	public int getLine() {
		return line;
	}

	@Override
	public String toString() {
		return contents;
	}

	public int getEndOffset() {
		return 0;
	}

	public int getOffset() {
		return 0;
	}

	public String getResourceName() {
		return null;
	}
}
