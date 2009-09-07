package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ITemplatesFacade;
import net.sf.lapg.templates.api.ILocatedEntity;

public class EvalNode extends Node {

	private ExpressionNode templateExpr;
	private final ExpressionNode templateLocation;

	public EvalNode(ExpressionNode expr, ExpressionNode templateId, String input, int line) {
		super(input, line);
		this.templateExpr = expr;
		this.templateLocation = templateId;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, ITemplatesFacade env) {
		try {
			Object toEvaluate = env.evaluate(templateExpr, context, false);
			String id;
			if( templateLocation != null ) {
				id = env.toString(env.evaluate(templateLocation, context, false), templateLocation);
			} else {
				id = toEvaluate instanceof ILocatedEntity ? ((ILocatedEntity)toEvaluate).getLocation() : null;
			}
			String templateCode = env.toString(toEvaluate, templateExpr);
			sb.append(env.evaluateTemplate(this, templateCode, id, context));
		} catch (EvaluationException ex) {
		}
	}
}
