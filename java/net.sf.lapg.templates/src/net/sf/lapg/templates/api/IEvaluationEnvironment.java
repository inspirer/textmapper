package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.ExpressionNode;

public interface IEvaluationEnvironment {

	public Object getVariable(String id);
	public void setVariable(String id, Object value);

	public Object getProperty(Object obj, String id, boolean searchVars) throws EvaluationException;
	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException;
	public Object getByIndex(Object obj, Object index) throws EvaluationException;
	public boolean toBoolean(Object o) throws EvaluationException;

	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException;
	public String executeTemplate(String name, Object context, Object[] arguments);

	public String getContextTitle(Object context);

	public void fireError(String error);
	public IStaticMethods getStaticMethods();
}