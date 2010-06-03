package net.sf.lapg.gen.options.ast;

// group ::= Lglobal scon '{' declarations '}' (normal)
// group ::= anno_kind '{' declarations '}' (normal)
// group ::= Ltypes '{' typedefs '}' (normal)
public class Group {

	private String title;
	private Object declarations;
	private Object kind;
	private Object typedefs;

	public String getTitle() {
		return title;
	}
	public Object getDeclarations() {
		return declarations;
	}
	public Object getKind() {
		return kind;
	}
	public Object getTypedefs() {
		return typedefs;
	}
}
