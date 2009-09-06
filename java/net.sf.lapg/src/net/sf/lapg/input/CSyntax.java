package net.sf.lapg.input;

import java.util.List;
import java.util.Map;

import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Lexem;

public class CSyntax implements Grammar {

	public static final String EOI = "eoi";
	public static final String INPUT = "input";
	public static final String ERROR = "error";
	public static final String OPTSUFFIX = "opt";

	private List<String> errors;

	private final CSymbol[] symbols;
	private final CRule[] rules;
	private final CPrio[] prios;
	private final Map<String, String> options;
	private final CLexem[] lexems;
	private final String templates;

	private final int myInput;
	private final int myError;
	private final int myTerms;

	public CSyntax(List<CSymbol> symbols, List<CRule> rules, List<CPrio> prios, Map<String, String> options,
			List<CLexem> lexems, String templates) {
		this.symbols = symbols.toArray(new CSymbol[symbols.size()]);
		this.rules = rules.toArray(new CRule[rules.size()]);
		this.prios = prios.toArray(new CPrio[prios.size()]);
		this.lexems = lexems.toArray(new CLexem[lexems.size()]);
		this.options = options;
		this.templates = templates;
		sortSymbols();
		enumerateAll();

		myInput = findSymbol(INPUT);
		myError = findSymbol(ERROR);
		int i = 0;
		for (; i < this.symbols.length && this.symbols[i].isTerm(); i++) {
			;
		}
		myTerms = i;
	}

	private int findSymbol(String name) {
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i].getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Inplace sort of symbols. [eoi term] [other terms] [all non-terminals]
	 */
	private void sortSymbols() {
		int first = 0, end = symbols.length - 1;

		while (first < end) {
			while (symbols[first].isTerm() && first < end) {
				first++;
			}
			while (!symbols[end].isTerm() && first < end) {
				end--;
			}
			if (first < end) {
				CSymbol ex = symbols[end];
				symbols[end] = symbols[first];
				symbols[first] = ex;
			}
		}
		if (symbols.length > 0 && !symbols[0].getName().equals(EOI)) {
			for (int i = 1; i < symbols.length; i++) {
				if (symbols[i].getName().equals(EOI)) {
					CSymbol ex = symbols[i];
					symbols[i] = symbols[0];
					symbols[0] = ex;
					break;
				}
			}
		}
	}

	private void enumerateAll() {
		for (int i = 0; i < symbols.length; i++) {
			symbols[i].index = i;
		}
		for (int i = 0; i < rules.length; i++) {
			rules[i].index = i;
		}
	}

	public CSyntax(List<String> errors) {
		this.errors = errors;
		lexems = null;
		options = null;
		prios = null;
		rules = null;
		symbols = null;
		templates = null;
		myTerms = 0;
		myInput = myError = -1;
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

	public CPrio[] getPriorities() {
		return prios;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public List<String> getErrors() {
		return errors;
	}

	public int getEoi() {
		return 0;
	}

	public int getError() {
		return myError;
	}

	public int getInput() {
		return myInput;
	}

	public int getTerminals() {
		return myTerms;
	}

	public Lexem[] getLexems() {
		return lexems;
	}

	public String getTemplates() {
		return templates;
	}

	public boolean hasActions() {
		for (CRule r : rules) {
			if (r.getAction() != null) {
				return true;
			}
		}
		return false;
	}

	public boolean hasLexemActions() {
		for (int i = 0; i < lexems.length; i++) {
			if (lexems[i].getAction() != null) {
				return true;
			}
		}
		return false;
	}
}
