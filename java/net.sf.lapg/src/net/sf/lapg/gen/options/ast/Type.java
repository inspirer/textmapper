package net.sf.lapg.gen.options.ast;

// type ::= identifier (normal)
// type ::= Luint (normal)
// type ::= Lstring (normal)
// type ::= Lidentifier (normal)
// type ::= Lqualified (normal)
// type ::= Lsymbol (normal)
// type ::= Lbool (normal)
// type ::= Lbool '(' string ',' string ')' (normal)
// type ::= Lset '(' strings ')' (normal)
// type ::= Lchoice '(' strings ')' (normal)
// type ::= Larray '(' type ')' (unknown)
// type ::= Lstruct '{' declarations '}' (normal)
public class Type {

	private String identifier;
	private Object trueVal;
	private Object falseVal;
	private Object strings;
	private Object type;
	private Object declarations;

	public String getIdentifier() {
		return identifier;
	}
	public Object getTrueVal() {
		return trueVal;
	}
	public Object getFalseVal() {
		return falseVal;
	}
	public Object getStrings() {
		return strings;
	}
	public Object getType() {
		return type;
	}
	public Object getDeclarations() {
		return declarations;
	}
}
