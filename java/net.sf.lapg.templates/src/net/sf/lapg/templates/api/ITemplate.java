package net.sf.lapg.templates.api;

public interface ITemplate extends ILocatedEntity {
	String getName();
	String getPackage();
	String apply(EvaluationContext context, IEvaluationEnvironment env, Object[] arguments) throws EvaluationException;
	String getOverridden();
}
