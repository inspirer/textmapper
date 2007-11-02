package net.sf.lapg.templates.model.xml;

import java.util.List;

import net.sf.lapg.templates.api.IEntity;

public class XmlNode implements IEntity {

	private String tagName;
	private List<XmlAttribute> attributes;
	private int line;

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

	public String getLocation() {
		return null;
	}

	public List<XmlAttribute> getAttributes() {
		return attributes;
	}

	public String getTitle() {
		return "tag " + tagName + " at line " + line;
	}
}
