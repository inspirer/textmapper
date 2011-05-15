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

import org.textway.templates.api.EvaluationException;
import org.textway.templates.objects.DefaultJavaIxObject;
import org.textway.templates.objects.IxObject;
import org.textway.templates.objects.JavaIxFactory;

import java.util.ArrayList;

public class XmlIxFactory extends JavaIxFactory {

	@Override
	public IxObject asObject(Object o) {
		if (o instanceof XmlNode) {
			return new XmlNodeIxObject((XmlNode) o);
		}
		return super.asObject(o);
	}

	private static class XmlNodeIxObject extends DefaultJavaIxObject {

		XmlNode node;

		private XmlNodeIxObject(XmlNode node) {
			super(node);
			this.node = node;
		}

		@Override
		public Object getByIndex(Object key) throws EvaluationException {
			if (false == key instanceof String) {
				return null;
			}

			if (((String) key).startsWith("@")) {
				String searchAttr = ((String) key).substring(1);
				if (node.getAttributes() != null) {
					for (XmlAttribute attr : node.getAttributes()) {
						if (attr.getName().equals(searchAttr)) {
							return attr;
						}
					}
				}
			}

			ArrayList<XmlNode> nodes = new ArrayList<XmlNode>();
			for (XmlNode o : node.getNodes()) {
				if (o.getTagName().equals(key)) {
					nodes.add(o);
				}
			}
			return nodes;
		}

		@Override
		public Object getProperty(String property) throws EvaluationException {
			if (property.equals("tagName")) {
				return node.getTagName();
			}
			if (property.equals("nodes")) {
				return node.getNodes();
			}
			if (property.equals("children")) {
				return node.getChildren();
			}
			if (property.equals("attrs")) {
				return node.getAttributes();
			}

			return getByIndex(property);
		}
	}

	;
}
