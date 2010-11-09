package org.textway.templates.ast;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.AstTree.TextSource;

public class InstanceOfNode extends ExpressionNode {

	private final ExpressionNode expr;
	private final String pattern;

	public InstanceOfNode(ExpressionNode node, String pattern, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.expr = node;
		this.pattern = pattern;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object element = env.evaluate(expr, context, true);
		if(element != null) {
			String name = element.getClass().getCanonicalName();
			if(matches(name, pattern)) {
				return Boolean.TRUE;
			}
			if(pattern.indexOf('.') >= 0) {
				return hasSupertype(element.getClass(), pattern);
			}
		}
		return Boolean.FALSE;
	}

	private static boolean hasSupertype(Class<? extends Object> class_, String className) {
		if(class_ == null) {
			return false;
		}
		if(matches(class_.getCanonicalName(), className)) {
			return true;
		}
		for(Class<? extends Object> i : class_.getInterfaces()) {
			if(hasSupertype(i, className)) {
				return true;
			}
		}
		return hasSupertype(class_.getSuperclass(), className);
	}

	private static boolean matches(String name, String pattern) {
		if(name == null) {
			return false;
		}
		if(pattern.indexOf('.') >= 0) {
			return name.equals(pattern);
		}
		if(name.indexOf('.') >= 0) {
			name = name.substring(name.lastIndexOf('.') + 1);
		}
		if(name.equals(pattern) || name.toLowerCase().equals(pattern)) {
			return true;
		}
		return false;
	}

	@Override
	public void toString(StringBuilder sb) {
		expr.toString(sb);
		sb.append(" is ");
		sb.append(pattern);
	}
}
