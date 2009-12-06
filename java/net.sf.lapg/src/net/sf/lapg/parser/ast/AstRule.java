package net.sf.lapg.parser.ast;

import java.util.List;
import java.util.Map;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRule extends Node {

	private final List<AstRuleSymbol> list;
	private final AstCode action;
	private final AstIdentifier priority;
	private final Map<String,Object> annotations;

	public AstRule(List<AstRuleSymbol> list, AstCode action, AstIdentifier priority, Map<String,Object> annotations, TextSource source,
			int offset, int endoffset) {
		super(source, offset, endoffset);
		this.list = list;
		this.action = action;
		this.priority = priority;
		this.annotations = annotations;
	}

	public List<AstRuleSymbol> getList() {
		return list;
	}

	public AstCode getAction() {
		return action;
	}

	public AstIdentifier getPriority() {
		return priority;
	}
	
	public Map<String, Object> getAnnotations() {
		return annotations;
	}
}
