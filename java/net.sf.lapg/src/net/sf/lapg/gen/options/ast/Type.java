package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

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
public class Type extends AstOptNode {

	private String identifier;
	private _String trueVal;
	private _String falseVal;
	private List<_String> strings;
	private Type type;
	private List<Declaration> declarations;

	public Type(String identifier, _String trueVal, _String falseVal, List<_String> strings, Type type, List<Declaration> declarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.identifier = identifier;
		this.trueVal = trueVal;
		this.falseVal = falseVal;
		this.strings = strings;
		this.type = type;
		this.declarations = declarations;
	}

	public String getIdentifier() {
		return identifier;
	}
	public _String getTrueVal() {
		return trueVal;
	}
	public _String getFalseVal() {
		return falseVal;
	}
	public List<_String> getStrings() {
		return strings;
	}
	public Type getType() {
		return type;
	}
	public List<Declaration> getDeclarations() {
		return declarations;
	}
}
