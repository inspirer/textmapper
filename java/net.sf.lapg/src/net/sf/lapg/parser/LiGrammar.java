package net.sf.lapg.parser;

import java.util.Map;

import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Lexem;
import net.sf.lapg.api.Prio;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;

public class LiGrammar implements Grammar {
	
	private final Symbol[] symbols;
	private final Rule[] rules;
	private final Prio[] priorities;
	private final Lexem[] lexems;
	
	private final Symbol[] inputs;
	private final Symbol eoi;
	private final Symbol error;
	
	private final Map<String, String> options;
	private final String templates;
	private final int terminals;
	private final boolean hasErrors;

	public LiGrammar(Symbol[] symbols, Rule[] rules, Prio[] priorities, Lexem[] lexems, Symbol[] inputs, Symbol eoi,
			Symbol error, Map<String, String> options, String templates, int terminals, boolean hasErrors) {
		this.symbols = symbols;
		this.rules = rules;
		this.priorities = priorities;
		this.lexems = lexems;
		this.inputs = inputs;
		this.eoi = eoi;
		this.error = error;
		this.options = options;
		this.templates = templates;
		this.terminals = terminals;
		this.hasErrors = hasErrors;
	}

	@Override
	public Symbol[] getSymbols() {
		return symbols;
	}

	@Override
	public Rule[] getRules() {
		return rules;
	}
	
	@Override
	public Prio[] getPriorities() {
		return priorities;
	}

	@Override
	public Lexem[] getLexems() {
		return lexems;
	}
	
	@Override
	public Symbol[] getInput() {
		return inputs;
	}
	
	@Override
	public Symbol getEoi() {
		return eoi;
	}

	@Override
	public Symbol getError() {
		return error;
	}

	@Override
	public Map<String, String> getOptions() {
		return options;
	}

	@Override
	public String getTemplates() {
		return templates;
	}

	@Override
	public int getTerminals() {
		return terminals;
	}

	@Override
	public boolean hasErrors() {
		return hasErrors;
	}

	@Override
	public boolean hasActions() {
		for (Rule r : rules) {
			if (r.getAction() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasLexemActions() {
		for (Lexem l : lexems) {
			if (l.getAction() != null) {
				return true;
			}
		}
		return false;
	}
}
