package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

// declaration ::= identifier ':' type modifiersopt defaultval optionslistopt (normal)
public class Declaration extends AstOptNode {

	private String identifier;
	private Type type;
	private List<Modifier> modifiersopt;
	private Defaultval defaultval;
	private List<Option> optionslistopt;

	public Declaration(TextSource input, int start, int end) {
		super(input, start, end);
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
