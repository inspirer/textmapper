package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Declaration extends AstOptNode {

	private String identifier;
	private Type type;
	private List<Modifier> modifiersopt;
	private Defaultval defaultval;
	private List<Option> optionslistopt;

	public Declaration(String identifier, Type type, List<Modifier> modifiersopt, Defaultval defaultval, List<Option> optionslistopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.type = type;
		this.modifiersopt = modifiersopt;
		this.defaultval = defaultval;
		this.optionslistopt = optionslistopt;
	}

	public String getIdentifier() {
		return identifier;
	}
	public Type getType() {
		return type;
	}
	public List<Modifier> getModifiersopt() {
		return modifiersopt;
	}
	public Defaultval getDefaultval() {
		return defaultval;
	}
	public List<Option> getOptionslistopt() {
		return optionslistopt;
	}
}
