package net.sf.lapg.gen.options.ast;

import java.util.List;

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
	private _String trueVal;
	private _String falseVal;
	private List<Object> strings;
	private Type type;
	private List<Object> declarations;

	public String getIdentifier() {
		return identifier;
	}
	public _String getTrueVal() {
		return trueVal;
	}
	public _String getFalseVal() {
		return falseVal;
	}
	public List<Object> getStrings() {
		return strings;
	}
	public Type getType() {
		return type;
	}
	public List<Object> getDeclarations() {
		return declarations;
	}
}
