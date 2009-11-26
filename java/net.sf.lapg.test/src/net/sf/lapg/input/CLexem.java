package net.sf.lapg.input;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CLexem implements ILocatedEntity, Lexem {

	private final CSymbol sym;
	private final String regexp;
	private final CAction action;
	private final int priority;
	private final int groups;

	private final String input;
	private final int line;

	public CLexem(CSymbol sym, String regexp, CAction action, int priority, int groups, String input, int line) {
		this.sym = sym;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
		this.groups = groups;
		this.input = input;
		this.line = line;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public CSymbol getSymbol() {
		return sym;
	}

	public String getRegexp() {
		return regexp;
	}

	public Action getAction() {
		return action;
	}

	public int getPriority() {
		return priority;
	}

	public int getGroups() {
		return groups;
	}
}
