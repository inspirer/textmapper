package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class QueryNode extends ExpressionNode {

	private ExpressionNode objectExpr;
	private String queryString;

	public QueryNode(ExpressionNode objectExpr, String queryString, int line) {
		super(line);
		this.objectExpr = objectExpr;
		this.queryString = queryString;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		Object object;
		if( objectExpr != null ) {
			object = env.evaluate(objectExpr, context, false);
		} else {
			object = context;
		}

		return env.getByQuery(object, queryString);
	}

	@Override
	public void toString(StringBuffer sb) {
		if( objectExpr != null ) {
			objectExpr.toString(sb);
			sb.append(".");
		}
		sb.append('`');
		sb.append(queryString);
		sb.append('`');
	}
}
