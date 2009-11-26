package net.sf.lapg.input;

import java.util.List;

import net.sf.lapg.api.Prio;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CPrio implements Prio, ILocatedEntity {

	private final int prio;
	private final CSymbol[] symbols;

	private final String input;
	private final int line;

	public CPrio(int prio, List<CSymbol> symbols, String input, int line) {
		this.prio = prio;
		this.symbols = symbols.toArray(new CSymbol[symbols.size()]);
		this.input = input;
		this.line = line;
	}

	public int getPrio() {
		return prio;
	}

	public CSymbol[] getSymbols() {
		return symbols;
	}

	public String getLocation() {
		return input + "," + line;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		switch (prio) {
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
		for (CSymbol s : symbols) {
			if (notfirst) {
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
