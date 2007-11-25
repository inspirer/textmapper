package net.sf.lapg.gen;

import java.util.ArrayList;

import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.impl.DefaultNavigationFactory;


public class GrammarNavigationFactory extends DefaultNavigationFactory {

	private final String templatePackage;

	public GrammarNavigationFactory(String templatePackage) {
		this.templatePackage = templatePackage;
	}

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
				return new ActionSymbol(array[(Integer)index], false, array.length - (Integer)index - 1, environment, templatePackage);
			} else {
				throw new EvaluationException("index object should be integer");
			}
		}

		public Object getProperty(Object obj, String id) throws EvaluationException {
			Rule rule = (Rule) obj;
			SymbolList result = new SymbolList();
			if( rule.getLeft().getName().equals(id)) {
				result.add(new ActionSymbol(rule.getLeft(), true, 0, environment, templatePackage));
			}

			Symbol[] right = rule.getRight();
			for( int i = 0; i < right.length; i++ ) {
				String name = right[i].getName();
				if( id.equals(name)) {
					result.add(new ActionSymbol(right[i], false, right.length - i - 1, environment, templatePackage));
				}
			}

			if( result.size() == 1 ) {
				return result.get(0);
			} else if( result.size() > 1 ) {
				return result;
			}
			throw new EvaluationException("do not know symbol `"+id+"`");
		}
	};

	private static class SymbolList extends ArrayList<ActionSymbol> {
		private static final long serialVersionUID = 1176522415409277309L;

		@Override
		public String toString() {
			// TODO some runtime exception
			return "<several objects>";
		}
	}
}
