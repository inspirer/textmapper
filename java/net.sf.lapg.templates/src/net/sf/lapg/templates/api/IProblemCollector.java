package net.sf.lapg.templates.api;

public interface IProblemCollector {

	public void fireError(ILocatedEntity referer, String error);
}
