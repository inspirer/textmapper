package net.sf.lapg.api;

import java.util.Map;

/**
 * Input grammar definition.
 */
public interface Grammar {

	Symbol[] getSymbols();
	Rule[] getRules();
	Prio[] getPriorities();

	Map<String, String> getOptions();

	Lexem[] getLexems();

	int getTerminals();
	int getInput();
	int getEoi();
	int getError();

	String getTemplates();

	boolean hasActions();
	boolean hasLexemActions();
	boolean hasErrors();
}
