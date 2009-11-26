package net.sf.lapg.test.oldparser;

import java.util.List;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CInputDef implements ILocatedEntity {

	private final CSymbol[] symbols;

	private final String input;
	private final int line;

	public CInputDef(List<CSymbol> symbols, String input, int line) {
		this.symbols = symbols.toArray(new CSymbol[symbols.size()]);
		this.input = input;
		this.line = line;
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
		sb.append("input");
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
