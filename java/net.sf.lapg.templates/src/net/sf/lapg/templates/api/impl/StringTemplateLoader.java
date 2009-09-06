package net.sf.lapg.templates.api.impl;

import net.sf.lapg.templates.api.ITemplateLoader;

/**
 * In-memory template loader. 
 */
public class StringTemplateLoader implements ITemplateLoader {

	String myContainer;
	String myContents;

	public StringTemplateLoader(String container, String contents) {
		this.myContainer = container;
		this.myContents = contents;
	}

	public String load(String containerName) {
		if( containerName.equals(myContainer)) {
			return myContents;
		}
		return null;
	}

}
