package net.sf.lapg.templates.model.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.INavigatableContainer;
import net.sf.lapg.templates.api.IPropertyContainer;

public class XmlNode extends XmlElement implements IPropertyContainer, INavigatableContainer {

	private String tagName;
	private List<XmlAttribute> attributes;
	private int line;
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
	public void toString(StringBuffer sb) {
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

	public Object getByIndex(Object key) {

		if( false == key instanceof String ) {
			return null;
		}

		if( ((String)key).startsWith("@") ) {
			String searchAttr = ((String)key).substring(1);
			if( attributes != null ) {
				for(XmlAttribute attr : attributes) {
					if( attr.getName().equals(searchAttr)) {
						return attr;
					}
				}
			}
		}

		ArrayList<XmlNode> nodes = new ArrayList<XmlNode>();
		if( data != null ) {
			for( Object o : data) {
				if( o instanceof XmlNode && ((XmlNode)o).tagName.equals(key) ) {
					nodes.add((XmlNode)o);
				}
			}
		}
		return nodes;
	}

	public Object getProperty(String property) {
		if( property.equals("tagName")) {
			return getTagName();
		}
		if( property.equals("nodes")) {
			return getNodes();
		}
		if( property.equals("attrs")) {
			return getAttributes();
		}

		if( data != null ) {
			for( Object o : data) {
				if( o instanceof XmlNode && ((XmlNode)o).tagName.equals(property) ) {
					return o;
				}
			}
		}
		return null;
	}

	public Object getByQuery(String queryString) throws EvaluationException {
		// TODO
		return null;
	}
}
