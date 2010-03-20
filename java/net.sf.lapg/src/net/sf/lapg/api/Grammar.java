package net.sf.lapg.api;

import java.util.List;
import java.util.Map;

/**
 * Input grammar definition.
 */
public interface Grammar {

	Symbol[] getSymbols();
	Rule[] getRules();
	Prio[] getPriorities();

	Map<String, Object> getOptions();

	Lexem[] getLexems();

	int getTerminals();
	Symbol[] getInput();
	Symbol getEoi();
	Symbol getError();

	String getTemplates();

	boolean hasActions();
	boolean hasLexemActions();
	boolean hasErrors();
	Map<Symbol,List<Rule>> getRulesBySymbol();
}
