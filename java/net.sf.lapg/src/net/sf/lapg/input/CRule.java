package net.sf.lapg.input;

import java.util.List;

public class CRule {
	CSymbol left;
	List<CSymbol> right;

	void setLeft(CSymbol sym) {
		this.left = sym;
	}
}
