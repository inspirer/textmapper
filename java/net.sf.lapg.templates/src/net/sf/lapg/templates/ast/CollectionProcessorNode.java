package net.sf.lapg.templates.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;


public class CollectionProcessorNode extends ExpressionNode {

	static final int COLLECT = 1;
	static final int COLLECTUNIQUE = 2;
	static final int REJECT = 3;
	static final int SELECT = 4;
	static final int FORALL = 5;
	static final int EXISTS = 6;
	static final int SORT = 7;

	private static final String[] INSTR_WORDS = new String[] { null, "collect", "collectUnique", "reject", "select", "forAll", "exists", "sort" };

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
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object select = env.evaluate(selectExpression, context, false);
		Object prevVar = context.getVariable(varName);
		try {
			Iterator<?> it = env.getCollectionIterator(select);
			if(it == null) {
				throw new EvaluationException("`" + selectExpression.toString() + "` should be array or collection (instead of "+select.getClass().getCanonicalName()+")");
			}

			if(instruction == SELECT || instruction == REJECT || instruction == COLLECT || instruction == COLLECTUNIQUE) {
				Collection<Object> result = instruction == COLLECTUNIQUE ? new LinkedHashSet<Object>() : new ArrayList<Object>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					if(instruction != COLLECT && instruction != COLLECTUNIQUE) {
						boolean b = env.toBoolean(val) ^ (instruction == REJECT);
						if(b) {
							result.add(curr);
						}
					} else {
						result.add(val);
					}
				}
				return instruction == COLLECTUNIQUE ? new ArrayList<Object>(result) : result;
			} else if(instruction == SORT) {
				List<Object> result = new ArrayList<Object>();
				final Map<Object, Comparable<Object>> sortKey = new HashMap<Object, Comparable<Object>>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					if(!(val instanceof Comparable<?>)) {
						throw new EvaluationException("`" + foreachExpr.toString() + "` should implement Comparable (instead of "+val.getClass().getCanonicalName()+")");
					}
					sortKey.put(curr, (Comparable<Object>)val);
					result.add(curr);
				}
				Object[] arr = result.toArray();
				Arrays.sort(arr, new Comparator<Object>() {
					public int compare(Object o1, Object o2) {
						if(o1 == null) {
							return o2 == null ? 0 : -1;
						}
						if(o2 == null) {
							return 1;
						}
						return sortKey.get(o1).compareTo(sortKey.get(o2));
					}
				});
				return arr;
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
