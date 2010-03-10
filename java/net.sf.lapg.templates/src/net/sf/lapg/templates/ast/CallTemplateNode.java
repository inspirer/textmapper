package net.sf.lapg.templates.ast;

import java.util.List;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.IQuery;
import net.sf.lapg.templates.api.ITemplate;

public class CallTemplateNode extends ExpressionNode {

	private final String templateId;
	private ExpressionNode templateIdExpr;
	private final ExpressionNode[] arguments;
	private final ExpressionNode selectExpr;
	private final boolean isStatement;

	public CallTemplateNode(String identifier, List<ExpressionNode> args, ExpressionNode selectExpr, String currentPackage, boolean isStatement, String input, int line) {
		super(input, line);
		this.isStatement = isStatement;
		this.arguments = args != null ? args.toArray(new ExpressionNode[args.size()]) : null;
		this.selectExpr = selectExpr;
		this.templateId = identifier;
	}

	public CallTemplateNode(ExpressionNode identifier, List<ExpressionNode> args, ExpressionNode selectExpr, String currentPackage, String input, int line) {
		this((String) null, args, selectExpr, currentPackage, false, input, line);
		this.templateIdExpr = identifier;
	}

	private String getTemplateId(EvaluationContext context) {
		return templateId != null && templateId.indexOf('.') == -1 && !templateId.equals("base") ? context.getCurrent().getPackage() + "." + templateId : templateId;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		EvaluationContext callContext = selectExpr != null ? new EvaluationContext(env.evaluate(selectExpr, context, false), context) : context;
		String tid = templateId != null ? getTemplateId(context) : (String/* TODO */) env.evaluate(templateIdExpr, context, false);

		Object[] args = null;
		if (arguments != null) {
			args = new Object[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				args[i] = env.evaluate(arguments[i], context, false);
			}
		}

		IBundleEntity t = null;
		boolean isBase = false;
		IBundleEntity current = callContext.getCurrent();
		if (tid.equals("base")) {
			if (current != null) {
				isBase = true;
				t = current.getBase();
				if (t == null) {
					env.fireError(this, "Cannot find base template for `" + current.getName() + "`");
				}
			}
		}
		if(!isBase) {
			t = env.loadEntity(tid, IBundleEntity.KIND_ANY, this);
		}
		if(t instanceof ITemplate) {
			return env.evaluate((ITemplate)t, callContext, args, this);
		} else if(t instanceof IQuery) {
			return env.evaluate((IQuery)t, callContext, args, this);
		} else {
			return "";
		}
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
