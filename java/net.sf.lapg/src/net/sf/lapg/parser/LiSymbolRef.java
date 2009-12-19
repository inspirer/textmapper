package net.sf.lapg.parser;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;

public class LiSymbolRef implements SymbolRef {
	
	Symbol target;
	String alias;

	public LiSymbolRef(Symbol target, String alias) {
		this.target = target;
		this.alias = alias;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

}
