package net.sf.lapg.templates.model.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlNode extends XmlElement {

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

	public List<XmlElement> getChildren() {
		return data;
	}

	public String getNodeDeclaration() {
		StringBuffer sb = new StringBuffer();
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
