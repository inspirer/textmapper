package net.sf.lapg.templates.api;

public interface IBundleEntity extends ILocatedEntity {

	public static final int KIND_ANY = 0;
	public static final int KIND_TEMPLATE = 1;
	public static final int KIND_QUERY = 2;

	/**
	 * @return KIND_TEMPLATE or KIND_QUERY
	 */
	int getKind();

	/**
	 * @return member name
	 */
	String getName();

	/**
	 * @return qualified name of package
	 */
	String getPackage();

	/**
	 * @return signature to map overrides
	 */
	String getSignature();

	/**
	 * Returns overridden member
	 */
	IBundleEntity getBase();

	/**
	 * Internal: Used for binding.
	 */
	void setBase(IBundleEntity base);
}
