package net.sf.lapg.input;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CSymbol implements ILocatedEntity {

	private int line;

	private String name;
	private String type;
	private boolean isTerm = false, isDefined = false;

	// non-term
	private List<CRule> rules;

	// term
	private CAction lexemAction;
	private int lexemPrio;

	public CSymbol(String name) {
		this.line = 0;
		this.name = name;
	}

	void setDefined( String type, int line ) {
		this.type = type;
		this.line = line;
		this.isDefined = true;
	}

	void setTerminal( String type, String regexp, int prio, CAction action, int line ) {
		setDefined(type, line);
		this.isTerm = true;
		this.lexemAction = action;
	}

	void addRules(List<CRule> rules) {
		if( this.rules == null ) {
			this.rules = new ArrayList<CRule>();
		}
		this.rules.addAll(rules);
		for( CRule r : rules ) {
			r.setLeft(this);
		}
	}

	public String getLocation() {
		return "line:" + line;
	}
}
