package net.sf.lapg.input;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CSymbol implements ILocatedEntity {

	private int line;

	private String name;
	private String type;
	private boolean isTerm = false, isDefined = false;

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
		this.lexemPrio = prio;
	}

	public String getLocation() {
		return "line:" + line;
	}

	public boolean isTerm() {
		return isTerm;
	}

	public boolean isDefined() {
		return isDefined;
	}

	public CAction getLexemAction() {
		return lexemAction;
	}

	public int getLexemPrio() {
		return lexemPrio;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( name == null ) {
			sb.append("<noname>");
		} else {
			sb.append(name);
		}
		if( type != null ) {
			sb.append(" (");
			sb.append(type);
			sb.append(")");
		}
		sb.append(" [");
		sb.append("term=");
		sb.append(isTerm);
		sb.append(", defined=");
		sb.append(isDefined);
		sb.append("]");
		return sb.toString();
	}
}
