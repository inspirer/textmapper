package net.sf.lapg.templates.api;

/**
 * Abstraction of engine which is able to load templates.
 */
public interface IBundleLoader {

	/**
	 * Default templates extension
	 */
	public static final String BUNDLE_EXT = ".ltp";

	/**
	 * @param bundleName
	 *            qualified name of templates bundle (separated with dots)
	 * @return contents of the file with templates
	 */
	TemplatesBundle load(String bundleName, IProblemCollector collector);
}
