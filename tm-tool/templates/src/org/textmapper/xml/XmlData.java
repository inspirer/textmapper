/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.xml;


public class XmlData extends XmlElement {

	private final CharSequence buffer;
	private final int start;
	private final int len;

	XmlData(CharSequence buffer, int start, int len) {
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(buffer.subSequence(start, len));
	}

	public String getTitle() {
		return "XMLDATA";
	}
}
