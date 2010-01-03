package net.sf.lapg.templates.api;

/**
 * Navigation strategy specifies how to take a property or call a method of an object. It can be registered for classes
 * or concrete instances.
 */
public interface INavigationStrategy<T> {

	/**
	 * Returns value of <propertyName> property of <obj>.
	 */
	public Object getProperty(T obj, String propertyName) throws EvaluationException;

	/**
	 * Returns a result of <obj>.methodName(args) call.
	 */
	public Object callMethod(T obj, String methodName, Object[] args) throws EvaluationException;

	/**
	 * Returns indexed value.
	 */
	public Object getByIndex(T obj, Object index) throws EvaluationException;

	/**
	 * Factory returns strategy for objects.
	 */
	public static interface Factory {

		/**
		 * Connects factory to the evaluation strategy.
		 */
		public void setEvaluationStrategy(IEvaluationStrategy strategy);

		/**
		 * Returns navigation strategy for object instance.
		 */
		public INavigationStrategy<?> getStrategy(Object o);
	}
}
