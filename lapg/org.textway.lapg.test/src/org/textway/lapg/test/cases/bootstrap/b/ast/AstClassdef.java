package org.textway.lapg.test.cases.bootstrap.b.ast;

import java.util.List;
import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;

public class AstClassdef extends AstNode implements IAstClassdefNoEoi {

	private AstID ID;
	private List<AstClassdeflistItem> classdeflistopt;
	private String Lextends;
	private String identifier;

	public AstClassdef(AstID ID, List<AstClassdeflistItem> classdeflistopt, String Lextends, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.ID = ID;
		this.classdeflistopt = classdeflistopt;
		this.Lextends = Lextends;
		this.identifier = identifier;
	}

	public AstID getID() {
		return ID;
	}
	public List<AstClassdeflistItem> getClassdeflistopt() {
		return classdeflistopt;
	}
	public String getLextends() {
		return Lextends;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (ID != null) {
			ID.accept(v);
		}
		if (classdeflistopt != null) {
			for (AstClassdeflistItem it : classdeflistopt) {
				it.accept(v);
			}
		}
		// TODO for Lextends
		// TODO for identifier
	}
}
