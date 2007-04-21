package net.sf.lapg.gen;

public class TargetLanguage {
	
	String id;
	String template;
	String defaultFile;
	
	public TargetLanguage(String id, String template, String defaultFile) {
		this.id = id;
		this.template = template;
		this.defaultFile = defaultFile;
	}
}
