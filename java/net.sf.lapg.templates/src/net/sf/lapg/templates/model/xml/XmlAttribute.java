package net.sf.lapg.templates.model.xml;


public class XmlAttribute extends XmlElement {

	private String name;
	private String value;
	private XmlNode container = null;

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

	void setContainer(XmlNode node) {
		this.container = node;
	}

	public String getTitle() {
		return (container == null ? "<unknown>" : container.getTitle()) + ".@" + name;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(name);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");
	}
}
