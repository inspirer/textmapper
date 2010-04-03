package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstDirective extends AstNode implements AstGrammarPart {

	private final String key;
	private final List<AstReference> symbols;

	public AstDirective(String key, List<AstReference> symbols, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.key = key;
		this.symbols = symbols;
	}

	public String getKey() {
		return key;
	}

	public List<AstReference> getSymbols() {
		return symbols;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (symbols != null) {
			for (AstReference id : symbols) {
				id.accept(v);
			}
		}
	}
}
