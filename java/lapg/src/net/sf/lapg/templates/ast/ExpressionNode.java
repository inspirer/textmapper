package net.sf.lapg.templates.ast;

public abstract class ExpressionNode extends Node {
	
	protected void emit(StringBuffer sb, Object context) {
		Object resolved = resolve(context);
		if( resolved != null )
			sb.append(resolved.toString());
	}
	
	public abstract Object resolve(Object context);
}