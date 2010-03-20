package net.sf.lapg.parser;

import net.sf.lapg.api.Symbol;

public class LiSymbol extends LiAnnotated implements Symbol {

	private int index;
	private final String name;
	private final String type;
	private final boolean isTerm;

	public LiSymbol(String name, String type, boolean isTerm) {
		super(null);
		this.name = name;
		this.type = type;
		this.isTerm = isTerm;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isDefined() {
		return true;
	}

	public boolean isTerm() {
		return isTerm;
	}
}
