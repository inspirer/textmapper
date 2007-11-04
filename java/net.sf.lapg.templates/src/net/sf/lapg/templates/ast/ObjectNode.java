package net.sf.lapg.templates.ast;

import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ObjectNode extends ExpressionNode {

	private HashMap<String,ExpressionNode> fields;

	public ObjectNode(HashMap<String,ExpressionNode> fields) {
		this.fields = fields;
	}

	@Override
	public Object evaluate(Object context, IEvaluationEnvironment env) throws EvaluationException {
		HashMap<String,Object> result = new HashMap<String,Object>();
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
			sb.append(entry.getValue().toString());
		}
		sb.append(']');
	}
}
