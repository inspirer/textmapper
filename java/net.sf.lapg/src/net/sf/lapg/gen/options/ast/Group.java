package net.sf.lapg.gen.options.ast;

import java.util.List;

// group ::= Lglobal scon '{' declarations '}' (normal)
// group ::= anno_kind '{' declarations '}' (normal)
// group ::= Ltypes '{' typedefs '}' (normal)
public class Group {

	private String title;
	private List<Object> declarations;
	private AnnoKind kind;
	private List<Object> typedefs;

	public String getTitle() {
		return title;
	}
	public List<Object> getDeclarations() {
		return declarations;
	}
	public AnnoKind getKind() {
		return kind;
	}
	public List<Object> getTypedefs() {
		return typedefs;
	}
}
