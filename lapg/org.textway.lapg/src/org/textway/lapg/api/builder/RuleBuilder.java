package org.textway.lapg.api.builder;

import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;

import java.util.Collection;

public interface RuleBuilder {

	SymbolRef addPart(String alias, Symbol sym, Collection<Symbol> unwanted, SourceElement origin);

	SymbolRef addHidden(String alias, Symbol sym, SourceElement origin);

	void setPriority(Symbol sym);

	RuleBuilder copy();

	Rule create();
}
