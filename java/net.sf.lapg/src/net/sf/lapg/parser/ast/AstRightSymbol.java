package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRightSymbol extends Node {

	private AstCode action;
	private String astName;
	private AstIdentifier symbol;

	public AstRightSymbol(AstCode action, String name, AstIdentifier symbol, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.action = action;
		this.astName = name;
		this.symbol = symbol;
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
}
