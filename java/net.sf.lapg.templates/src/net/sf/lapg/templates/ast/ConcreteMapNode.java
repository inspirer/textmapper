package net.sf.lapg.templates.ast;

import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ConcreteMapNode extends ExpressionNode {

	private HashMap<ExpressionNode,ExpressionNode> fields;

	public ConcreteMapNode(HashMap<ExpressionNode,ExpressionNode> fields, int line) {
		super(line);
		this.fields = fields;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationEnvironment env) throws EvaluationException {
		HashMap<Object,Object> result = new HashMap<Object,Object>();
		for( Map.Entry<ExpressionNode,ExpressionNode> entry : fields.entrySet() ) {
			result.put(env.evaluate(entry.getKey(), context, false), env.evaluate(entry.getValue(), context, false) );
		}
		return result;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('[');
		boolean notFirst = false;
		for( Map.Entry<ExpressionNode,ExpressionNode> entry : fields.entrySet() ) {
			if(notFirst) {
				sb.append(",");
			} else {
				notFirst = true;
			}
			entry.getKey().toString(sb);
			sb.append("->");
			entry.getValue().toString(sb);
		}
		sb.append(']');
	}
}
