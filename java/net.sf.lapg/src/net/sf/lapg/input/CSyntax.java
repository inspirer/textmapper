package net.sf.lapg.input;

import java.util.List;

public class CSyntax {

	public static final String EOI = "eoi";
	public static final String INPUT = "input";

	private List<String> errors;

	private CSymbol[] symbols;
	private CRule[] rules;
	private CPrio[] prios;
	private COption[] options;

	public CSyntax(List<CSymbol> symbols, List<CRule> rules, List<CPrio> prios, List<COption> options) {
		this.symbols = symbols.toArray(new CSymbol[symbols.size()]);
		this.rules = rules.toArray(new CRule[rules.size()]);
		this.prios = prios.toArray(new CPrio[prios.size()]);
		this.options = options.toArray(new COption[options.size()]);
		sortSymbols();
		enumerateAll();
	}

	/**
	 *  Inplace sort of symbols. [eoi term] [other terms] [all non-terminals]
	 */
	private void sortSymbols() {
		int first = 0, end = symbols.length - 1;

		while( first < end ) {
			while( symbols[first].isTerm() && first < end ) {
				first++;
			}
			while( !symbols[end].isTerm() && first < end) {
				end--;
			}
			if( first < end ) {
				CSymbol ex = symbols[end];
				symbols[end] = symbols[first];
				symbols[first] = ex;
			}
		}
		if( symbols.length > 0 && !symbols[0].getName().equals(EOI) ) {
			for( int i = 1; i < symbols.length; i++ ) {
				if( symbols[i].getName().equals(EOI) ) {
					CSymbol ex = symbols[i];
					symbols[i] = symbols[0];
					symbols[0] = ex;
					break;
				}
			}
		}
	}

	private void enumerateAll() {
		for( int i = 0; i < symbols.length; i++ ) {
			symbols[i].index = i;
		}
		for( int i = 0; i < rules.length; i++ ) {
			rules[i].index = i;
		}
	}

	public CSyntax(List<String> errors) {
		this.errors = errors;
	}

	public boolean hasErrors() {
		return errors != null;
	}

	public CSymbol[] getSymbols() {
		return symbols;
	}

	public CRule[] getRules() {
		return rules;
	}

	public CPrio[] getPrios() {
		return prios;
	}

	public COption[] getOptions() {
		return options;
	}

	public List<String> getErrors() {
		return errors;
	}
}
