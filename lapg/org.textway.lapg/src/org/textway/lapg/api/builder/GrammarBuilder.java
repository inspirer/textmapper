package org.textway.lapg.api.builder;

import org.textway.lapg.api.*;
import org.textway.lapg.api.regex.RegexPart;

import java.util.Collection;

public interface GrammarBuilder {

	Symbol addSymbol(int kind, String name, String type, SourceElement origin);

	Symbol addSoftSymbol(String name, Symbol softClass, SourceElement origin);
	
	Symbol getEoi();

	NamedPattern addPattern(String name, RegexPart regexp, SourceElement origin);

	Lexem addLexem(int kind, Symbol sym, RegexPart regexp, int groups, int priority, Lexem classLexem, SourceElement origin);

	InputRef addInput(Symbol inputSymbol, boolean hasEoi, SourceElement origin);

	Prio addPrio(int prio, Collection<Symbol> symbols, SourceElement origin);

	RuleBuilder rule(String alias, Symbol left, SourceElement origin);

	Grammar create();
}
