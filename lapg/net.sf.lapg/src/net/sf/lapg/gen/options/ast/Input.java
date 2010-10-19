package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Input extends AstOptNode {

	private List<Declaration> declarations;

	public Input(List<Declaration> declarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.declarations = declarations;
	}

	public List<Declaration> getDeclarations() {
		return declarations;
	}
}
