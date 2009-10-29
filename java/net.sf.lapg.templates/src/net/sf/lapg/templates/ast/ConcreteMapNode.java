package net.sf.lapg.templates.ast;

import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationStrategy;

public class ConcreteMapNode extends ExpressionNode {

	private HashMap<String,ExpressionNode> fields;

	public ConcreteMapNode(HashMap<String,ExpressionNode> fields, String input, int line) {
		super(input, line);
		this.fields = fields;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		HashMap<Object,Object> result = new HashMap<Object,Object>();
		for( Map.Entry<String,ExpressionNode> entry : fields.entrySet() ) {
			result.put(entry.getKey(), env.evaluate(entry.getValue(), context, false) );
		}
		return result;
	}

	@Override
	public void toString(StringBuffer sb) {
		sb.append('[');
		boolean notFirst = false;
		for( Map.Entry<String,ExpressionNode> entry : fields.entrySet() ) {
			if(notFirst) {
				sb.append(",");
			} else {
				notFirst = true;
			}
			sb.append(entry.getKey());
			sb.append(":");
			entry.getValue().toString(sb);
		}
		sb.append(']');
	}
}
