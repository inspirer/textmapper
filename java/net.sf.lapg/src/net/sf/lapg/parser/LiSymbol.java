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

	@Override
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public boolean isTerm() {
		return isTerm;
	}
}
