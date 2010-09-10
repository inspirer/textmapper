package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class StringConstraint extends AstOptNode implements IConstraint {

	private List<_String> strings;

	public StringConstraint(List<_String> strings, TextSource input, int start, int end) {
		super(input, start, end);
		this.strings = strings;
	}

	public List<_String> getStrings() {
		return strings;
	}
}
