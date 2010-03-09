package net.sf.lapg.templates.api;

/**
 * Loaded and validated template.
 */
public interface ITemplate extends IBundleEntity {

	/**
	 * Evaluates template in context and environment.
	 * @return result of invocation
	 */
	String apply(EvaluationContext context, IEvaluationStrategy env, Object[] arguments) throws EvaluationException;
}
