package net.sf.lapg.templates.api;

public interface IEvaluationCache {

	void cache(Object value, Object... keys);

	Object lookup(Object... keys);
}
