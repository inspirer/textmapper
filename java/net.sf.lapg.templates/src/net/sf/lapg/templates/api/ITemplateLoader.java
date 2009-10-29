package net.sf.lapg.templates.api;

/**
 * Abstraction of engine which is able to load templates.
 */
public interface ITemplateLoader {

	/**
	 * Default templates extension
	 */
	public static final String CONTAINER_EXT = ".ltp";

	/**
	 * @param containerName
	 *            qualified name of templates package (separated with dots)
	 * @return contents of the file with templates
	 */
	TemplatesPackage load(String containerName, IProblemCollector collector);
}
