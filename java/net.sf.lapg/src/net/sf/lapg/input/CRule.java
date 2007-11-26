package net.sf.lapg.input;

import java.util.List;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Rule;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INamedEntity;

public class CRule implements ILocatedEntity, INamedEntity, Rule {

	private CSymbol left;
	private final CSymbol[] right;
	private final CAction action;
	private final CSymbol priority;
	private final String input;
	private final int line;
	int index;

	public CRule(List<CSymbol> right, CAction action, CSymbol priority, String input, int line) {
		this.right = right != null ? right.toArray(new CSymbol[right.size()]) : new CSymbol[0];
		this.action = action;
		this.priority = priority;
		this.index = -1;
		this.input = input;
		this.line = line;
	}

	void setLeft(CSymbol sym) {
		this.left = sym;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public CSymbol getLeft() {
		return left;
	}

	public CSymbol[] getRight() {
		return right;
	}

	public Action getAction() {
		return action;
	}

	public int getPriority() {
		if( priority != null ) {
			return priority.getIndex();
		}
		for( int i = right.length-1; i >= 0; i --) {
			if( right[i].isTerm()) {
				return right[i].getIndex();
			}
		}
		return -1;
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
		if( priority != null ) {
			sb.append(" << ");
			sb.append(priority.getName());
		}
		return sb.toString();
	}

	public String getTitle() {
		return "Rule `" + toString() + "`";
	}
}
