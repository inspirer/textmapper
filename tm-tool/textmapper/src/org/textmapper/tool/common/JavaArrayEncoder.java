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
package org.textmapper.tool.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 2/22/12
 */
public class JavaArrayEncoder {

	private static final int MAX_LENGTH = 0xfff0;

	private final List<List<String>> result = new ArrayList<List<String>>();
	private final StringBuilder sb;
	private final int lineWidth;
	private List<String> current;
	private int lengthInUtf8;

	public JavaArrayEncoder(int width) {
		lineWidth = width;
		sb = new StringBuilder(width + 16);
		newString();
	}

	public List<List<String>> getResult() {
		newLine();
		if (result.size() == 1 && current.isEmpty()) {
			current.add("\"\"");
		}
		return result;
	}

	public void appendInt(int i) {
		append((char) (i & 0xffff));
		append((char) ((i >> 16) & 0xffff));
	}

	public void appendChar(int i) {
		assert i >= 0 && i <= 0xffff;
		append((char) i);
	}

	public void appendShort(int i) {
		assert i >= Short.MIN_VALUE && i <= Short.MAX_VALUE;
		append((char) ((short) i));
	}

	private void newString() {
		current = new ArrayList<String>();
		result.add(current);
		sb.setLength(0);
		sb.append("\"");
		lengthInUtf8 = 0;
	}

	private void newLine() {
		if (sb.length() > 1) {
			sb.append("\"");
			current.add(sb.toString());
			sb.setLength(1);
		}
	}

	private void append(char c) {
		if (lengthInUtf8 >= MAX_LENGTH) {
			newLine();
			newString();
		} else if (sb.length() > lineWidth) {
			newLine();
		}

		lengthInUtf8 += charUtf8Length(c);
		if (c > 0xff) {
			sb.append(c < 0x1000 ? "\\u0" : "\\u");
			sb.append(Integer.toHexString(c));
		} else {
			sb.append("\\");
			sb.append(Integer.toOctalString(c));
		}
	}

	private int charUtf8Length(char value) {
		if (value == 0) return 2;
		if (value <= 0x7F) return 1;
		if (value <= 0x7FF) return 2;
		return 3;
	}
}
