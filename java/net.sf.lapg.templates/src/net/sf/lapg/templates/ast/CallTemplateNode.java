package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ITemplatesFacade;

public class CallTemplateNode extends ExpressionNode {

	private String templateId;

	private ExpressionNode templateIdExpr;

	private ExpressionNode[] arguments;

	private ExpressionNode selectExpr;

	private final boolean isStatement;

	public CallTemplateNode(String identifier, List<ExpressionNode> args, ExpressionNode selectExpr, String currentPackage, boolean isStatement, String input, int line) {
		super(input, line);
		this.isStatement = isStatement;
		this.arguments = args != null ? args.toArray(new ExpressionNode[args.size()]) : null;
		this.selectExpr = selectExpr;
		this.templateId = identifier != null && identifier.indexOf('.') == -1 && !identifier.equals("base") ? currentPackage + "." + identifier : identifier;
	}

	public CallTemplateNode(ExpressionNode identifier, List<ExpressionNode> args, ExpressionNode selectExpr, String currentPackage, String input, int line) {
		this((String) null, args, selectExpr, currentPackage, false, input, line);
		this.templateIdExpr = identifier;
	}

	@Override
	public Object evaluate(EvaluationContext context, ITemplatesFacade env) throws EvaluationException {
		EvaluationContext callContext = selectExpr != null ? new EvaluationContext(env.evaluate(selectExpr, context, false), context) : context;
		String tid = templateId != null ? templateId : (String/* TODO */) env.evaluate(templateIdExpr, context, false);

		Object[] args = null;
		if (arguments != null) {
			args = new Object[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				args[i] = env.evaluate(arguments[i], context, false);
			}
		}
		return env.executeTemplate(tid, callContext, args, this);
	}

	@Override
	public void toString(StringBuffer sb) {
		if (!isStatement) {
			if(selectExpr != null) {
				selectExpr.toString(sb);
			} else {
				sb.append("self");
			}
			sb.append("->");
			if (templateId != null) {
				sb.append(templateId);
			} else {
				sb.append("(");
				templateIdExpr.toString(sb);
				sb.append(")");
			}
			sb.append("(");
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					if (i > 0) {
						sb.append(",");
					}
					arguments[i].toString(sb);
				}
			}
			sb.append(")");
		} else {
			// statement
			sb.append("call ");
			sb.append(templateId);
			if (arguments != null && arguments.length > 0) {
				sb.append("(");
				for (int i = 0; i < arguments.length; i++) {
					if (i > 0) {
						sb.append(",");
					}
					arguments[i].toString(sb);
				}
				sb.append(")");
			}
			if(selectExpr != null) {
				sb.append(" for ");
				selectExpr.toString(sb);
			}
		}
	}
}
