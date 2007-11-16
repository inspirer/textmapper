package net.sf.lapg.templates.api;

public interface ITemplateLoader {

	public static final String CONTAINER_EXT = ".ltp";

	String load(String containerName);
}
