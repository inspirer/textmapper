package net.sf.lapg.templates.model.xml;

import java.io.UnsupportedEncodingException;

public class XmlData extends XmlElement {

	private byte[] buffer;
	private int start;
	private int len;

	XmlData(byte[] buffer, int start, int len) {
		this.buffer = buffer;
		this.start = start;
		this.len = len;
	}

	@Override
	public void toString(StringBuffer sb) {
		try {
			sb.append(new String(buffer, start, len, "utf-8"));
		} catch (UnsupportedEncodingException e) {
		}
	}

	public String getTitle() {
		return "XMLDATA";
	}
}
