package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Group extends AstOptNode {

	private String title;
	private AnnoKind kind;
	private List<Declaration> declarations;
	private List<Typedef> typedefs;

	public Group(String title, AnnoKind kind, List<Declaration> declarations, List<Typedef> typedefs, TextSource input, int start, int end) {
		super(input, start, end);
		this.title = title;
		this.kind = kind;
		this.declarations = declarations;
		this.typedefs = typedefs;
	}

	public String getTitle() {
		return title;
	}
	public AnnoKind getKind() {
		return kind;
	}
	public List<Declaration> getDeclarations() {
		return declarations;
	}
	public List<Typedef> getTypedefs() {
		return typedefs;
	}
}
