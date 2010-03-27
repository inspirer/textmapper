package net.sf.lapg.parser.ast;

import java.util.Map;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRuleSymbol extends AstNode {

	private final AstCode action;
	private final String alias;
	private final AstIdentifier symbol;
	private final Map<String,Object> annotations;

	public AstRuleSymbol(AstCode action, String alias, AstIdentifier symbol, Map<String,Object> annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.action = action;
		this.alias = alias;
		this.symbol = symbol;
		this.annotations = annotations;
	}

	public AstCode getCode() {
		return action;
	}

	public AstIdentifier getSymbol() {
		return symbol;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public Map<String, Object> getAnnotations() {
		return annotations;
	}
}
