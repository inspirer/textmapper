package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.TextSource;

public class AstRuleRight extends Node {

	private List<AstRightSymbol> list;
	private AstCode action;
	private AstIdentifier priority;

	public AstRuleRight(List<AstRightSymbol> list, AstCode action, AstIdentifier priority, TextSource source,
			int offset, int endoffset) {
		super(source, offset, endoffset);
		this.list = list;
		this.action = action;
		this.priority = priority;
	}

	public List<AstRightSymbol> getList() {
		return list;
	}

	public AstCode getAction() {
		return action;
	}

	public AstIdentifier getPriority() {
		return priority;
	}
}
