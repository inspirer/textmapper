package net.sf.lapg.templates.ast;

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class CollectionProcessorNode extends ExpressionNode {

	static final int COLLECT = 1;
	static final int REJECT = 2;
	static final int SELECT = 3;
	static final int FORALL = 4;
	static final int EXISTS = 5;

	private static final String[] INSTR_WORDS = new String[] { null, "collect", "reject", "select", "forAll", "exists" };

	private final ExpressionNode selectExpression;
	private final int instruction;
	private final String varName;
	private final ExpressionNode foreachExpr;

	public CollectionProcessorNode(ExpressionNode forExpr, int instruction, String varName, ExpressionNode foreachExpr, String input, int line) {
		super(input,line);
		this.selectExpression = forExpr;
		this.instruction = instruction;
		this.varName = varName;
		this.foreachExpr = foreachExpr;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		Object select = env.evaluate(selectExpression, context, false);
		Object prevVar = context.getVariable(varName);
		try {
			Iterator<?> it = env.getCollectionIterator(select);
			if(it == null) {
				throw new EvaluationException("`" + selectExpression.toString() + "` should be array or collection");
			}

			if(instruction == SELECT || instruction == REJECT || instruction == COLLECT) {
				ArrayList<Object> result = new ArrayList<Object>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					if(instruction != COLLECT) {
						boolean b = env.toBoolean(val) ^ (instruction == REJECT);
						if(b) {
							result.add(curr);
						}
					} else {
						result.add(val);
					}
				}
				return result;
			} else {
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					boolean b = env.toBoolean(val);
					if( b && instruction == EXISTS) {
						return true;
					}
					if(!b && instruction == FORALL ) {
						return false;
					}
				}
				return instruction == FORALL;
			}
		} finally {
			context.setVariable(varName, prevVar);
		}
	}

	@Override
	public void toString(StringBuffer sb) {
		selectExpression.toString(sb);
		sb.append(".");
		sb.append(INSTR_WORDS[instruction]);
		sb.append("(");
		sb.append(varName);
		sb.append("|");
		foreachExpr.toString(sb);
		sb.append(")");
	}
}
