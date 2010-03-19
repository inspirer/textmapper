package net.sf.lapg.templates.model.xml;

import net.sf.lapg.templates.model.xml.XmlTree.TextSource;

public class XmlModel {

	public static XmlNode load(String content) {
		XmlTree<XmlNode> tree = XmlTree.parse(new TextSource(".xml", content.toCharArray(), 1));
		return tree.getRoot();
	}
}
