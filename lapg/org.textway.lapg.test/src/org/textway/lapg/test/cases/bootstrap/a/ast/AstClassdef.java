package org.textway.lapg.test.cases.bootstrap.a.ast;

import java.util.List;
import org.textway.lapg.test.cases.bootstrap.a.SampleATree.TextSource;

public class AstClassdef extends AstNode implements IAstClassdefNoEoi {

	private String identifier;
	private List<AstClassdef> classdeflistopt;

	public AstClassdef(String identifier, List<AstClassdef> classdeflistopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.classdeflistopt = classdeflistopt;
	}

	public String getIdentifier() {
		return identifier;
	}
	public List<AstClassdef> getClassdeflistopt() {
		return classdeflistopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for identifier
		if (classdeflistopt != null) {
			for (AstClassdef it : classdeflistopt) {
				it.accept(v);
			}
		}
	}
}
