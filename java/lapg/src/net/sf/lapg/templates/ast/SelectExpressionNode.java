package net.sf.lapg.templates.ast;

import java.util.Map;

public class SelectExpressionNode extends ExpressionNode {
	String id;

	public SelectExpressionNode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Object resolve(Object context) {
		if( context instanceof Map) {
			return ((Map)context).get(id);
		}
		return null;
	}
}
