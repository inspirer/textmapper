package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.ExpressionNode;

public interface IEvaluationEnvironment {

	public Object getVariable(String id);
	public void setVariable(String id, Object value);

	public Object getProperty(Object obj, String id) throws EvaluationException;
	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException;
	public Object getByIndex(Object obj, Object index) throws EvaluationException;
	public Object getByQuery(Object obj, String query) throws EvaluationException;
	public boolean toBoolean(Object o) throws EvaluationException;

	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException;
	public String executeTemplate(ILocatedEntity referer, String name, Object context, Object[] arguments);
	public String executeTemplate(String name, Object context, Object[] arguments);
	public String evaluateTemplate(ILocatedEntity referer, String template, Object context);

	public String getTitle(Object context);

	public void fireError(ILocatedEntity referer, String error);
	public IStaticMethods getStaticMethods();
}