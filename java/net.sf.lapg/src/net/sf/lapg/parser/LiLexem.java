package net.sf.lapg.parser;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.api.Symbol;

public class LiLexem implements Lexem {

	private final Symbol sym;
	private final String regexp;
	private final int groups;
	private final int priority;
	private final Action action;
	
	public LiLexem(Symbol sym, String regexp, int groups, int priority, Action action) {
		this.sym = sym;
		this.regexp = regexp;
		this.groups = groups;
		this.priority = priority;
		this.action = action;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public int getGroups() {
		return groups;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getRegexp() {
		return regexp;
	}

	@Override
	public Symbol getSymbol() {
		return sym;
	}

}
