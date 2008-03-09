package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;


public class FileNode extends CompoundNode {

	private final ExpressionNode targetNameExpr;

	public FileNode(ExpressionNode expressionNode, String inputName, int line) {
		super(inputName, line);
		this.targetNameExpr = expressionNode;
	}

	@Override
	protected void emit(StringBuffer sb, EvaluationContext context, IEvaluationEnvironment env) {
		StringBuffer file = new StringBuffer();
		try {
			String fileName = env.toString(env.evaluate(targetNameExpr, context, false), targetNameExpr);
			super.emit(file, context, env);

			env.createFile(fileName, file.toString());
		} catch (EvaluationException ex) {
			/* ignore, skip if */
		}
	}
}
