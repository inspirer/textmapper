/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlNode extends XmlElement {

	private final String tagName;
	private final List<XmlAttribute> attributes;
	private final int line;
	private List<XmlElement> data = null;
	private List<XmlNode> nodes = null;

	public XmlNode(String tagName, List<XmlAttribute> attributes, int line) {
		this.tagName = tagName;
		this.attributes = attributes;
		this.line = 0;

		if( attributes != null ) {
			for( XmlAttribute attr : attributes ) {
				attr.setContainer(this);
			}
		}
	}

	public String getTagName() {
		return tagName;
	}

	public List<XmlAttribute> getAttributes() {
		return attributes;
	}

	Map<String,XmlAttribute> attributesMap;

	public Map<String,XmlAttribute> getAttributesMap() {
		if( attributesMap == null ) {
			attributesMap = new HashMap<String,XmlAttribute>();
			for( XmlAttribute attr : attributes) {
				attributesMap.put(attr.getName(), attr);
			}
		}
		return attributesMap;
	}

	public List<XmlNode> getNodes() {
		if( nodes == null ) {
			nodes = new ArrayList<XmlNode>();
			if( data != null ) {
				for( Object o : data) {
					if( o instanceof XmlNode ) {
						nodes.add((XmlNode)o);
					}
				}
			}
		}
		return nodes;
	}

	public String getTitle() {
		return tagName + ":" + line;
	}

	void setData(List<XmlElement> list) {
		this.data = list;
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append("<");
		sb.append(tagName);
		if( attributes != null ) {
			for(XmlAttribute attr : attributes) {
				sb.append(" ");
				attr.toString(sb);
			}
		}
		if( data == null || data.size() == 0) {
			sb.append("/>");
		} else {
			sb.append(">");
			for( XmlElement elem : data ) {
				elem.toString(sb);
			}
			sb.append("</");
			sb.append(tagName);
			sb.append(">");
		}

	}

	public List<XmlElement> getChildren() {
		return data;
	}

	public String getNodeDeclaration() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(tagName);
		if( attributes != null ) {
			for(XmlAttribute attr : attributes) {
				sb.append(" ");
				attr.toString(sb);
			}
		}
		sb.append(">");
		return sb.toString();
	}
}
