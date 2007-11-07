package net.sf.lapg.input;

import java.util.Collections;
import java.util.List;

public class CRule {

	private CSymbol left;
	private List<CSymbol> right;
	private CAction action;
	private CSymbol priority;

	public CRule(List<CSymbol> right, CAction action, CSymbol priority) {
		this.right = right != null ? right : Collections.<CSymbol>emptyList();
		this.action = action;
		this.priority = priority;
	}

	void setLeft(CSymbol sym) {
		this.left = sym;
	}
}
