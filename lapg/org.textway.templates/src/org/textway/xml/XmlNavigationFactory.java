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
package org.textway.xml;

import java.util.ArrayList;

import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.INavigationStrategy;
import org.textway.templates.api.impl.DefaultNavigationFactory;

public class XmlNavigationFactory extends DefaultNavigationFactory {

	@Override
	public INavigationStrategy getStrategy(Object o) {

		if( o instanceof XmlNode ) {
			return xmlNavigation;
		}

		return super.getStrategy(o);
	}

	private final INavigationStrategy xmlNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Object obj, Object key) throws EvaluationException {
			if( false == key instanceof String ) {
				return null;
			}

			XmlNode node = (XmlNode)obj;
			if( ((String)key).startsWith("@") ) {
				String searchAttr = ((String)key).substring(1);
				if( node.getAttributes() != null ) {
					for(XmlAttribute attr : node.getAttributes()) {
						if( attr.getName().equals(searchAttr)) {
							return attr;
						}
					}
				}
			}

			ArrayList<XmlNode> nodes = new ArrayList<XmlNode>();
			for( XmlNode o : node.getNodes()) {
				if( o.getTagName().equals(key) ) {
					nodes.add(o);
				}
			}
			return nodes;
		}

		public Object getProperty(Object obj, String property) throws EvaluationException {
			XmlNode node = (XmlNode)obj;

			if( property.equals("tagName")) {
				return node.getTagName();
			}
			if( property.equals("nodes")) {
				return node.getNodes();
			}
			if( property.equals("children")) {
				return node.getChildren();
			}
			if( property.equals("attrs")) {
				return node.getAttributes();
			}

			return getByIndex(obj, property);
		}
	};
}
