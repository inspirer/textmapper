package net.sf.lapg.input;

import java.util.List;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CSymbol implements ILocatedEntity {

	protected int line;

	private String name;
	private boolean isTerm = false, isDefined = false;
	private List<CRule> rules;

	public CSymbol(String name) {
		this.line = 0;
		this.name = name;
	}

	void intDefine( int line ) {
		this.line = line;
		this.isDefined = true;
	}

	void intTerminal() {
		this.isTerm = true;
	}

	void intAddRule(CRule rule) {

	}

	public String getLocation() {
		return "line:" + line;
	}
}
