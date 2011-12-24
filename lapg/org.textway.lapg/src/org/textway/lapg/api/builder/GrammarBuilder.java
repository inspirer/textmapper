package org.textway.lapg.api.builder;

import org.textway.lapg.api.*;
import org.textway.lapg.api.regex.RegexPart;

import java.util.Collection;

public interface GrammarBuilder {

	Symbol addSymbol(int kind, String name, String type);

	Symbol addSoftSymbol(String name, Symbol softClass);
	
	Symbol getEoi();

	NamedPattern addPattern(String name, RegexPart regexp);

	Lexem addLexem(int kind, Symbol sym, RegexPart regexp, int groups, int priority, Lexem classLexem);

	InputRef addInput(Symbol inputSymbol, boolean hasEoi);

	Prio addPrio(int prio, Collection<Symbol> symbols);

	RuleBuilder rule(String alias, Symbol left);

	Grammar create();
}
