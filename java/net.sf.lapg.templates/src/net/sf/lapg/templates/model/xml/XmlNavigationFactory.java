package net.sf.lapg.templates.model.xml;

import java.util.ArrayList;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.impl.DefaultNavigationFactory;

public class XmlNavigationFactory extends DefaultNavigationFactory {

	@Override
	public INavigationStrategy getStrategy(Object o) {

		if( o instanceof XmlNode ) {
			return xmlNavigation;
		}

		return super.getStrategy(o);
	}

	private INavigationStrategy xmlNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			// TODO Auto-generated method stub
			return null;
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
			if( property.equals("attrs")) {
				return node.getAttributes();
			}

			return getByIndex(obj, property);
		}
	};
}
