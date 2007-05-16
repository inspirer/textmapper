package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.ExpressionNode;

public interface IEvaluationEnvironment {

	public Object getVariable(String id);
	public void setVariable(String id, Object value);

	public Object getProperty(Object obj, String id, boolean searchVars);
	public Object callMethod(Object obj, String methodName, Object[] args);
	public Object getByIndex(Object obj, Object index);
	public boolean toBoolean(Object o);

	public Object evaluate(ExpressionNode expr, Object context, boolean permitNull) throws EvaluationException;
	public String executeTemplate(String name, Object context, Object[] arguments);

	public void fireError(String error);
}