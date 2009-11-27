package net.sf.lapg.parser;

import net.sf.lapg.api.Prio;
import net.sf.lapg.api.Symbol;

public class LiPrio implements Prio {

	private final int prio;
	private final LiSymbol[] symbols;
	
	public LiPrio(int prio, LiSymbol[] symbols) {
		this.prio = prio;
		this.symbols = symbols;
	}

	@Override
	public int getPrio() {
		return prio;
	}

	@Override
	public Symbol[] getSymbols() {
		return symbols;
	}
}
