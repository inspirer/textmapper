package net.sf.lapg.gen;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ActionSymbol {

	private final Symbol symbol;
	private final boolean isLeft;
	private final int rightOffset;
	private final IEvaluationEnvironment environment;
	private final String templatePackage;

	public ActionSymbol(Symbol symbol, boolean isLeft, int rightOffset, IEvaluationEnvironment environment, String templatePackage) {
		this.symbol = symbol;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.environment = environment;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		if( isLeft ) {
			return environment.executeTemplate(templatePackage+".leftSym", new EvaluationContext(symbol), null);
		} else {
			return environment.executeTemplate(templatePackage+".stackSym", new EvaluationContext(symbol), new Integer[] {rightOffset});
		}
	}
}
