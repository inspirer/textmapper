package net.sf.lapg.input;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CLexem implements ILocatedEntity {

	private final CSymbol sym;
	private final String regexp;
	private final CAction action;
	private final int priority;
	private final int line;

	public CLexem(CSymbol sym, String regexp, CAction action, int priority, int line) {
		this.sym = sym;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
		this.line = line;
	}

	public String getLocation() {
		return "line:" + line;
	}

	public CSymbol getSymbol() {
		return sym;
	}

	public String getRegexp() {
		return regexp;
	}

	public CAction getAction() {
		return action;
	}

	public int getPriority() {
		return priority;
	}
}
