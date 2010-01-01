package net.sf.lapg.parser;

import java.util.Map;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;

public class LiSymbolRef extends LiAnnotated implements SymbolRef {

	Symbol target;
	String alias;

	public LiSymbolRef(Symbol target, String alias, Map<String,Object> annotations) {
		super(annotations);
		this.target = target;
		this.alias = alias;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

}
