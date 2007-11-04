package net.sf.lapg.templates.model.xml;

import java.util.ArrayList;
import java.util.List;

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
				attr.container = this;
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
		return "tag " + tagName + " at line " + line;
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
}
