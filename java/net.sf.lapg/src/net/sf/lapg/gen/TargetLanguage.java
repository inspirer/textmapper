package net.sf.lapg.gen;

public class TargetLanguage {

	private final String id;
	private final String templatePackage;

	public TargetLanguage(String id, String templatePackage) {
		this.id = id;
		this.templatePackage = templatePackage;
	}

	public String getId() {
		return id;
	}

	public String getTemplatePackage() {
		return templatePackage;
	}
}
