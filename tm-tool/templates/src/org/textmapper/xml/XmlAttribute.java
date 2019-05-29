/**
 * Copyright 2002-2019 Evgeny Gryaznov
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


public class XmlAttribute extends XmlElement {

	private final String name;
	private final String value;
	private XmlNode container = null;

	public XmlAttribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	void setContainer(XmlNode node) {
		this.container = node;
	}

	public String getTitle() {
		return (container == null ? "<unknown>" : container.getTitle()) + ".@" + name;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(name);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");
	}
}
