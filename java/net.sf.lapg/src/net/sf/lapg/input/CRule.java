package net.sf.lapg.input;

import java.util.Collections;
import java.util.List;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CRule implements ILocatedEntity {

	private CSymbol left;
	private final List<CSymbol> right;
	private final CAction action;
	private final CSymbol priority;
	private final int line;
	int index;

	public CRule(List<CSymbol> right, CAction action, CSymbol priority, int line) {
		this.right = right != null ? right : Collections.<CSymbol>emptyList();
		this.action = action;
		this.priority = priority;
		this.line = line;
		this.index = -1;
	}

	void setLeft(CSymbol sym) {
		this.left = sym;
	}

	public String getLocation() {
		return "line:" + line;
	}

	public CSymbol getLeft() {
		return left;
	}

	public List<CSymbol> getRight() {
		return right;
	}

	public CAction getAction() {
		return action;
	}

	public CSymbol getPriority() {
		return priority;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( left.getName() == null ) {
			sb.append("<noname>");
		} else {
			sb.append(left.getName());
		}
		sb.append(" ::=");
		for( CSymbol s : right ) {
			sb.append(" ");
			if( s.getName() == null ) {
				sb.append("{}");
			} else {
				sb.append(s.getName());
			}
		}
		if( action != null ) {
			sb.append(" {}");
		}
		if( priority != null ) {
			sb.append(" << ");
			sb.append(priority.getName());
		}
		return sb.toString();
	}
}
