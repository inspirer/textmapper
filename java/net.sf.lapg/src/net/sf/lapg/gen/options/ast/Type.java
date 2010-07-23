package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Type extends AstOptNode {

	private _String trueVal;
	private _String falseVal;
	private String identifier;
	private Boolean Commaopt;
	private List<_String> strings;
	private Type type;
	private List<Declaration> declarations;

	public Type(_String trueVal, _String falseVal, String identifier, Boolean Commaopt, List<_String> strings, Type type, List<Declaration> declarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.trueVal = trueVal;
		this.falseVal = falseVal;
		this.identifier = identifier;
		this.Commaopt = Commaopt;
		this.strings = strings;
		this.type = type;
		this.declarations = declarations;
	}

	public _String getTrueVal() {
		return trueVal;
	}
	public _String getFalseVal() {
		return falseVal;
	}
	public String getIdentifier() {
		return identifier;
	}
	public Boolean getCommaopt() {
		return Commaopt;
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
