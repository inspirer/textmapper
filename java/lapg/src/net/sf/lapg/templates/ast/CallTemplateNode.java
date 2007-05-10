package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.ExecutionEnvironment;

public class CallTemplateNode extends Node {
	
	String identifier;
	List<ExpressionNode> arguments;
	ExpressionNode selectExpr;

	public CallTemplateNode(String identifier, List<ExpressionNode> args, ExpressionNode selectExpr) {
		this.identifier = identifier;
		this.arguments = args;
		this.selectExpr = selectExpr;
	}
	
	protected void emit(StringBuffer sb, Object context, ExecutionEnvironment env) {
	}
}
