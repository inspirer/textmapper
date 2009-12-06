package net.sf.lapg.parser.ast;

import java.util.Map;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRuleSymbol extends Node {

	private final AstCode action;
	private final String astName;
	private final AstIdentifier symbol;
	private final Map<String,Object> annotations;

	public AstRuleSymbol(AstCode action, String name, AstIdentifier symbol, Map<String,Object> annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.action = action;
		this.astName = name;
		this.symbol = symbol;
		this.annotations = annotations;
	}

	public AstCode getCode() {
		return action;
	}

	public AstIdentifier getSymbol() {
		return symbol;
	}
	
	public String getAstName() {
		return astName;
	}
	
	public Map<String, Object> getAnnotations() {
		return annotations;
	}
}
