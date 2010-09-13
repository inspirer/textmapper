package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class StringConstraint extends AstOptNode implements IConstraint {

	private Boolean kind;
	private List<_String> strings;
	private String identifier;

	public StringConstraint(Boolean kind, List<_String> strings, String identifier, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.strings = strings;
		this.identifier = identifier;
	}

	public Boolean getKind() {
		return kind;
	}
	public List<_String> getStrings() {
		return strings;
	}
	public String getIdentifier() {
		return identifier;
	}
}
