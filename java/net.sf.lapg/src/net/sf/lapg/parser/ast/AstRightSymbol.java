package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.TextSource;

public class AstRightSymbol extends Node {

	private AstCode action;
	private AstIdentifier symbol;

	public AstRightSymbol(AstCode action, AstIdentifier symbol, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.action = action;
		this.symbol = symbol;
	}

	public AstCode getCode() {
		return action;
	}

	public AstIdentifier getSymbol() {
		return symbol;
	}
}
