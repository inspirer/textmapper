package net.sf.lapg.gen.options.ast;

// declaration ::= identifier ':' type modifiersopt defaultval optionslistopt (normal)
public class Declaration {

	private String identifier;
	private Object type;
	private Object modifiersopt;
	private Object defaultval;
	private Object optionslistopt;

	public String getIdentifier() {
		return identifier;
	}
	public Object getType() {
		return type;
	}
	public Object getModifiersopt() {
		return modifiersopt;
	}
	public Object getDefaultval() {
		return defaultval;
	}
	public Object getOptionslistopt() {
		return optionslistopt;
	}
}
