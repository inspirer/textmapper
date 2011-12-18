package org.textway.lapg.api.builder;

import org.textway.lapg.api.Rule;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;

import java.util.Collection;

public interface RuleBuilder {

	SymbolRef addPart(String alias, Symbol sym, Collection<Symbol> unwanted);

	SymbolRef addHidden(String alias, Symbol sym);

	void setPriority(Symbol sym);

	RuleBuilder copy();

	Rule create();
}
