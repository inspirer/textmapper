package net.sf.lapg.input;

import java.util.List;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CPrio implements ILocatedEntity {

	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int NONASSOC = 3;

	private int prio;
	private List<CSymbol> symbols;
	private int line;

	public CPrio(int prio, List<CSymbol> symbols, int line) {
		this.prio = prio;
		this.symbols = symbols;
		this.line = line;
	}

	public int getPrio() {
		return prio;
	}

	public List<CSymbol> getSymbols() {
		return symbols;
	}

	public String getLocation() {
		return "line:" + line;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		switch(prio) {
		case LEFT:
			sb.append("left");
			break;
		case RIGHT:
			sb.append("right");
			break;
		case NONASSOC:
			sb.append("nonassoc");
			break;
		}
		sb.append("=[");
		boolean notfirst = false;
		for( CSymbol s : symbols ) {
			if( notfirst ) {
				sb.append(", ");
			} else {
				notfirst = true;
			}
			sb.append(s.getName());
		}
		sb.append("]");
		return sb.toString();
	}
}
