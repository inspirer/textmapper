package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class StringConstraint extends AstOptNode implements IConstraint {

	private Boolean kind;
	private List<_String> strings;

	public StringConstraint(Boolean kind, List<_String> strings, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.strings = strings;
	}

	public Boolean getKind() {
		return kind;
	}
	public List<_String> getStrings() {
		return strings;
	}
}
