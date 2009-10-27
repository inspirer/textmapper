package net.sf.lapg.parser;

import java.util.List;

import net.sf.lapg.parser.ast.AstRoot;

public class LapgInput {

	private final TextSource source;
	private final AstRoot root;
	private final int templatesStart;
	private final List<Object> errors;
	
	public LapgInput(TextSource source, AstRoot root, int templatesStart,
			List<Object> errors) {
		this.source = source;
		this.root = root;
		this.templatesStart = templatesStart;
		this.errors = errors;
	}
	
	public TextSource getSource() {
		return source;
	}
	
	public AstRoot getRoot() {
		return root;
	}
	
	public int getTemplatesStart() {
		return templatesStart;
	}
	
	public List<Object> getErrors() {
		return errors;
	}
}
