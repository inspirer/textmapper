package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

// group ::= Lglobal scon '{' declarations '}' (normal)
// group ::= anno_kind '{' declarations '}' (normal)
// group ::= Ltypes '{' typedefs '}' (normal)
public class Group extends AstOptNode {

	private String title;
	private List<Declaration> declarations;
	private AnnoKind kind;
	private List<Typedef> typedefs;

	public Group(String title, List<Declaration> declarations, AnnoKind kind, List<Typedef> typedefs, TextSource input, int start, int end) {
		super(input, start, end);
		this.title = title;
		this.declarations = declarations;
		this.kind = kind;
		this.typedefs = typedefs;
	}

	public String getTitle() {
		return title;
	}
	public List<Declaration> getDeclarations() {
		return declarations;
	}
	public AnnoKind getKind() {
		return kind;
	}
	public List<Typedef> getTypedefs() {
		return typedefs;
	}
}
