package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstNonTerm extends Node implements AstGrammarPart {

	private AstIdentifier name;
	private String type;
	private List<AstRuleRight> right;

	public AstNonTerm(AstIdentifier name, String type, List<AstRuleRight> right, TextSource source, int offset,
			int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.right = right;
	}

	public AstIdentifier getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<AstRuleRight> getRight() {
		return right;
	}
}
