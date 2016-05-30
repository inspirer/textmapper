/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.objects.DefaultJavaIxObject;
import org.textmapper.templates.objects.IxObject;
import org.textmapper.templates.objects.JavaIxFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
		public Object getByIndex(SourceElement caller, Object key) throws EvaluationException {
			if (!(key instanceof String)) {
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

			return node.getNodes().stream().filter(o -> o.getTagName().equals
					(key)).collect(Collectors.toList());
		}

		@Override
		public Object getProperty(SourceElement caller, String property) throws EvaluationException {
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

			return getByIndex(caller, property);
		}
	}
}
