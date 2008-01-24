package net.sf.lapg.api;

import java.util.Map;


public interface Grammar {

	public Symbol[] getSymbols();
	public Rule[] getRules();
	public Prio[] getPriorities();
	public Map<String,String> getOptions();
	public Lexem[] getLexems();

	public int getTerminals();
	public int getInput();
	public int getEoi();
	public int getError();

	public String getTemplates();

	public boolean hasActions();
	public boolean hasLexemActions();
}
