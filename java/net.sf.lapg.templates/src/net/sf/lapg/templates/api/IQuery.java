package net.sf.lapg.templates.api;

public interface IQuery extends ILocatedEntity {

	/**
	 * @return template name
	 */
	String getName();

	/**
	 * @return qualified name of template's package
	 */
	String getPackage();

	/**
	 * Evaluates template in context and environment.
	 * @return result of invocation
	 */
	Object invoke(EvaluationContext context, IEvaluationStrategy env, Object[] arguments) throws EvaluationException;
}
