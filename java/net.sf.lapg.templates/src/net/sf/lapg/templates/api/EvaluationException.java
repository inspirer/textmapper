package net.sf.lapg.templates.api;


/**
 *  Some expression cannot be evaluated, all errors are shown.
 */
public class EvaluationException extends Exception {

	private static final long serialVersionUID = -1507473514714934980L;

	public EvaluationException(String message) {
		super(message);
	}
}
