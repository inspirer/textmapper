package net.sf.lapg.templates.model.xml;

import java.util.List;

public class XmlModel {

	public static XmlNode load(String content) {
		Parser p = new Parser();
		List<XmlElement> result = p.parse(content);
		XmlNode root = new XmlNode("<root>", null, 1);
		root.setData(result);
		return root;
	}

}
