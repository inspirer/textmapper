package net.sf.lapg.gen.options.ast;

import java.util.List;

// declaration ::= identifier ':' type modifiersopt defaultval optionslistopt (normal)
public class Declaration {

	private String identifier;
	private Type type;
	private List<Object> modifiersopt;
	private Defaultval defaultval;
	private List<Object> optionslistopt;

	public String getIdentifier() {
		return identifier;
	}
	public Type getType() {
		return type;
	}
	public List<Object> getModifiersopt() {
		return modifiersopt;
	}
	public Defaultval getDefaultval() {
		return defaultval;
	}
	public List<Object> getOptionslistopt() {
		return optionslistopt;
	}
}
