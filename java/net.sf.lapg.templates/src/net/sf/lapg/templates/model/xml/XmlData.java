package net.sf.lapg.templates.model.xml;


public class XmlData extends XmlElement {

	private char[] buffer;
	private int start;
	private int len;

	XmlData(char[] buffer, int start, int len) {
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append(new String(buffer, start, len));
	}

	public String getTitle() {
		return "XMLDATA";
	}
}
