package net.sf.lapg.gen;

import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.impl.DefaultNavigationFactory;


public class GrammarNavigationFactory extends DefaultNavigationFactory {

	@Override
	public INavigationStrategy getStrategy(Object o) {

		if( o instanceof Rule) {
			return ruleNavigation;
		}

		return super.getStrategy(o);
	}

	private INavigationStrategy ruleNavigation = new INavigationStrategy() {

		public Object callMethod(Object obj, String methodName, Object[] args) throws EvaluationException {
			return javaNavigation.callMethod(obj, methodName, args);
		}

		public Object getByIndex(Object obj, Object index) throws EvaluationException {
			Symbol[] array = ((Rule)obj).getRight();
			if( index instanceof Integer ) {
				return array[(Integer)index];
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			return javaNavigation.getProperty(obj, id);
		}

	};

}
