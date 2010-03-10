package net.sf.lapg.templates.api;

import java.util.Iterator;

import net.sf.lapg.templates.ast.ExpressionNode;

/**
 * Defines environment for evaluating set of templates.
 */
public interface IEvaluationStrategy extends INavigationStrategy<Object>, IProblemCollector, IStreamHandler {

	Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException;

	String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, ILocatedEntity referer);

	Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) throws EvaluationException;

	IBundleEntity loadEntity(String qualifiedName, int kind, ILocatedEntity referer);

	String eval(ILocatedEntity referer, String template, String templateId, EvaluationContext context);

	boolean toBoolean(Object o) throws EvaluationException;

	Iterator<?> getCollectionIterator(Object o);

	String toString(Object o, ExpressionNode referer) throws EvaluationException;

	String getTitle(Object object);
}