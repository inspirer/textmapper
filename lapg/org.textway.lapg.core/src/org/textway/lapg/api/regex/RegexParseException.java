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
package org.textway.lapg.api.regex;

public class RegexParseException extends Exception {
	private static final long serialVersionUID = -8052552834958196703L;

	private final int errorOffset;

	public RegexParseException(String message, int offset) {
		super(message);
		this.errorOffset = offset;
	}

	public int getErrorOffset() {
		return errorOffset;
	}
}
