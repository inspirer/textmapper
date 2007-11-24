package net.sf.lapg.gen;

import net.sf.lapg.api.Symbol;


public class ActionSymbol {
	private final Symbol symbol;
	private final boolean isLeft;
	private final int rightOffset;

	public ActionSymbol(Symbol symbol, boolean isLeft, int rightOffset) {
		this.symbol = symbol;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
	}

	@Override
	public String toString() {
		if( isLeft ) {
			return "lapg_gg.sym";
		} else if( symbol.getType() != null ) {
			return "(("+symbol.getType()+")lapg_m[lapg_head-"+rightOffset+"].sym)";
		} else {
			return "lapg_m[lapg_head-"+rightOffset+"].sym";
		}
	}
}
