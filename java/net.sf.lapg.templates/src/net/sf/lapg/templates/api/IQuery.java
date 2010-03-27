package net.sf.lapg.templates.api;

public interface IQuery extends IBundleEntity {

	/**
	 * Evaluates query in context and environment.
	 * @return result of invocation
	 */
	Object invoke(EvaluationContext context, IEvaluationStrategy env, Object[] arguments) throws EvaluationException;
}
