package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class SwitchNode extends Node {

	private final List<CaseNode> cases;
	private final ExpressionNode expression;
	private final List<Node> elseInstructions;

	public SwitchNode(ExpressionNode expression, List<CaseNode> cases, List<Node> elseInstructions, String input, int line) {
		super(input, line);
		this.expression = expression;
		this.cases = cases;
		this.elseInstructions = elseInstructions;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationStrategy env) {
		try {
			Object value = env.evaluate(expression, context, false);
			for( CaseNode n : cases ) {
				Object caseValue = env.evaluate(n.getExpression(), context, false);
				if( ConditionalNode.safeEqual(value, caseValue)) {
					n.emit(sb, context, env);
					return;
				}
			}
			for (Node n : elseInstructions) {
				n.emit(sb, context, env);
			}
		} catch( EvaluationException ex ) {
			/* ignore, statement will be skipped */
		}
	}
}
