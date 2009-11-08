package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstDirective extends Node implements AstGrammarPart {

	private final String key;
	private final List<AstIdentifier> symbols;
	
	public AstDirective(String key, List<AstIdentifier> symbols, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.key = key;
		this.symbols = symbols;
	}
	
	public String getKey() {
		return key;
	}

	public List<AstIdentifier> getSymbols() {
		return symbols;
	}
}
