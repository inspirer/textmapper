package net.sf.lapg.templates.api;

import java.util.Iterator;

import net.sf.lapg.templates.ast.ExpressionNode;

public interface IEvaluationEnvironment {

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

	public String executeTemplate(ILocatedEntity referer, String name, EvaluationContext context, Object[] arguments);

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments);

	public String evaluateTemplate(ILocatedEntity referer, String template, String templateId, EvaluationContext context);

	public String getTitle(Object object);

	public void fireError(ILocatedEntity referer, String error);

	public void loadPackage(ILocatedEntity referer, String packageName);

	public void createFile(String name, String contents);
}