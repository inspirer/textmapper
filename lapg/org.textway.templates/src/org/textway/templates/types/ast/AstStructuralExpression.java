package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstStructuralExpression extends AstNode implements IAstExpression {

	private List<String> name;
	private List<AstMapEntriesItem> mapEntriesopt;
	private List<IAstExpression> expressionListopt;

	public AstStructuralExpression(List<String> name, List<AstMapEntriesItem> mapEntriesopt, List<IAstExpression> expressionListopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.name = name;
		this.mapEntriesopt = mapEntriesopt;
		this.expressionListopt = expressionListopt;
	}

	public List<String> getName() {
		return name;
	}
	public List<AstMapEntriesItem> getMapEntriesopt() {
		return mapEntriesopt;
	}
	public List<IAstExpression> getExpressionListopt() {
		return expressionListopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (mapEntriesopt != null) {
			for (AstMapEntriesItem it : mapEntriesopt) {
				it.accept(v);
			}
		}
		if (expressionListopt != null) {
			for (IAstExpression it : expressionListopt) {
				it.accept(v);
			}
		}
	}
}
