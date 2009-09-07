package net.sf.lapg.templates.model.xml;

import java.util.List;

public class XmlModel {

	public static XmlNode load(String content) {
		XmlParser p = new XmlParser();
		List<XmlElement> result = p.parse(content);
		XmlNode root = new XmlNode("<root>", null, 1);
		root.setData(result);
		return root;
	}

}
