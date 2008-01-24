package net.sf.lapg.gen;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ActionSymbol {

	final Symbol symbol;
	final boolean isLeft;
	final int rightOffset;
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
		return environment.executeTemplate(templatePackage+".symbol", new EvaluationContext(this), null);
	}
}
