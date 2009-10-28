package net.sf.lapg.templates.api;

import java.util.Iterator;

import net.sf.lapg.templates.ast.ExpressionNode;

/**
 * Defines environment for evaluating set of templates.
 */
public interface ITemplatesFacade extends IProblemCollector {

	/**
	 *   <obj> . <propertyName>
	 */
	public Object getProperty(Object obj, String propertyName) throws EvaluationException;

	/**
	 *   <obj> . <methodName> ( <args> )
	 */
	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException;

	/**
	 *   <obj> [ <index> ]
	 */
	public Object getByIndex(Object obj, Object index) throws EvaluationException;

	/**
	 *    <obj> ? .. : ..
	 */
	public boolean toBoolean(Object o) throws EvaluationException;

	public Iterator<?> getCollectionIterator(Object o);

	public String toString(Object o, ExpressionNode referer) throws EvaluationException;

	public Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException;

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer);

	public String evaluateTemplate(ILocatedEntity referer, String template, String templateId, EvaluationContext context);

	public String getTitle(Object object);

	public void createFile(String name, String contents);
}