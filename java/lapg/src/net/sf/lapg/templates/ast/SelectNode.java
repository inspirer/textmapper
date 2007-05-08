package net.sf.lapg.templates.ast;

import java.util.Map;

public class SelectNode extends ExpressionNode {

	ExpressionNode object;
	String identifier;
	
	public SelectNode(ExpressionNode select, String identifier) {
		this.object = select;
		this.identifier = identifier;
	}

	public Object resolve(Object context) {
		if( object != null )
			context = object.resolve(context);
		if( context == null )
			return null;

		if( context instanceof Map) {
			return ((Map)context).get(identifier);
		}
		return null;
	}
}
