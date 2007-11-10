package net.sf.lapg.input;

import net.sf.lapg.api.Lexem;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CLexem implements ILocatedEntity, Lexem {

	private final CSymbol sym;
	private final String regexp;
	private final CAction action;
	private final int priority;
	private final int line;
	private final int groups;

	public CLexem(CSymbol sym, String regexp, CAction action, int priority, int groups, int line) {
		this.sym = sym;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
		this.line = line;
		this.groups = groups;
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

	public String getAction() {
		return action != null ? action.getContents() : null;
	}

	public int getPriority() {
		return priority;
	}

	public int getGroups() {
		return groups;
	}
}
