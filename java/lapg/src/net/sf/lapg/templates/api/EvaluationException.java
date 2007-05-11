package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.ExpressionNode;

public class EvaluationException extends Exception {

	private static final long serialVersionUID = -1507473514714934980L;

	public EvaluationException(ExpressionNode expr, Object context, String message, Throwable th) {
		super("Evaluation of `"+expr.toString()+"` failed for " + context.getClass().getCanonicalName() + ": " + message, th);
	}
	
	public EvaluationException(String message) {
		super(message);
	}
}
