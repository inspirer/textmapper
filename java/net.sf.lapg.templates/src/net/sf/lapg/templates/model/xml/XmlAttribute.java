package net.sf.lapg.templates.model.xml;

import net.sf.lapg.templates.api.IEntity;

public class XmlAttribute implements IEntity {

	private String name;
	private String value;
	XmlNode container = null;

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

	public String getLocation() {
		return null;
	}

	public String getTitle() {
		return "attribute " + name + " of " + (container == null ? "<unknown>" : container.getTitle());
	}
}
