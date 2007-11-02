package net.sf.lapg.templates.model.xml;

import java.util.List;

public class XmlModel {

	public static XmlNode load(String content) {
		Parser p = new Parser();
		List<?> result = p.parse(content);
		XmlNode root = new XmlNode("<root>", null, 1);
		root.data = result;
		return root;
	}

}
