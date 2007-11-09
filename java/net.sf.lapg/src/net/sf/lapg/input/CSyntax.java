package net.sf.lapg.input;

import java.util.List;

public class CSyntax {

	public static final String EOI = "eoi";
	public static final String INPUT = "input";

	private List<String> errors;

	private List<CSymbol> symbols;
	private List<CRule> rules;
	private List<CPrio> prios;
	private List<COption> options;

	public CSyntax(List<CSymbol> symbols, List<CRule> rules, List<CPrio> prios, List<COption> options) {
		this.symbols = symbols;
		this.rules = rules;
		this.prios = prios;
		this.options = options;
	}

	public CSyntax(List<String> errors) {
		this.errors = errors;
	}

	public boolean isParsed() {
		return errors == null;
	}

	public List<CSymbol> getSymbols() {
		return symbols;
	}

	public List<CRule> getRules() {
		return rules;
	}

	public List<CPrio> getPrios() {
		return prios;
	}

	public List<COption> getOptions() {
		return options;
	}
}
