package net.sf.lapg.templates.api;

public interface INavigationStrategy {

	// <obj>.id
	public Object getProperty(Object obj, String id) throws EvaluationException;

	// <obj>.methodName(args)
	public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException;

	// <obj>[index]
	public Object getByIndex(Object obj, Object index) throws EvaluationException;


	public static interface Factory {
		public INavigationStrategy getStrategy(Object o);
	}
}
