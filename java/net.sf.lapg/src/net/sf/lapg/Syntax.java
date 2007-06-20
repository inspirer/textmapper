package net.sf.lapg;

import java.util.Map;


public interface Syntax {
	Grammar getGrammar();
	Lexem[] getLexems();
	Map<String,String> getOptions();
}
