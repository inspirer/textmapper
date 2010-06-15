package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Input extends AstOptNode {

	private List<Group> groups;

	public Input(List<Group> groups, TextSource input, int start, int end) {
		super(input, start, end);
		this.groups = groups;
	}

	public List<Group> getGroups() {
		return groups;
	}
}
