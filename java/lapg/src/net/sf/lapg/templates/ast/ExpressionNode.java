package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;

public abstract class ExpressionNode extends Node {
	
	protected void emit(StringBuffer sb, Object context, ExecutionEnvironment env) {
		Object resolved = resolve(context, env);
		if( resolved != null )
			sb.append(resolved.toString());
	}
	
	public abstract Object resolve(Object context, ExecutionEnvironment env);
}