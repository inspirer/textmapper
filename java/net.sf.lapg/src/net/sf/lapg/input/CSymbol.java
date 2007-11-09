package net.sf.lapg.input;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.input.Parser.ParseException;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CSymbol implements ILocatedEntity {

	private final String name;
	private final List<CLexem> lexems = new ArrayList<CLexem>();

	private int line;
	private String type;
	private boolean isTerm = false, isDefined = false;

	public CSymbol(String name) {
		this.line = 0;
		this.name = name;
	}

	private void setDefined( String ntype, int nline, boolean nisterm ) throws ParseException {
		if( isDefined ) {
			if( nisterm != isTerm ) {
				throw new ParseException(
						(isTerm ?
								"redeclaring terminal `" + name + "` as non-terminal"
								: "redeclaring non-terminal `" + name + "` as terminal")
						+ " at line " + nline + " (previously declared at " + this.line + ")");
			}
			if( this.type != null && ntype != null && !this.type.equals(ntype) ) {
				throw new ParseException("redeclaring type for `"+name+"` at line " + nline + " (previously declared at line " + this.line + ")");
			}
		} else {
			line = nline;
			isDefined = true;
			isTerm = nisterm;
		}
		if( type == null ) {
			type = ntype;
		}
	}

	void setTerminal( String type, int line, CLexem lexem ) throws ParseException {
		if( name.equals(CSyntax.INPUT) ) {
			throw new ParseException("cannot declare terminal with name `"+name+"` (reserved non-terminal) at line " + line);
		}

		setDefined(type, line, true);
		if( lexem != null ) {
			lexems.add(lexem);
		}
	}

 	void setNonTerminal( String type, int line ) throws ParseException {
		setDefined(type, line, false);
	}

	public String getLocation() {
		return "line:" + line;
	}

	public boolean isTerm() {
		return isTerm;
	}

	public boolean isDefined() {
		return isDefined;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<CLexem> getLexems() {
		return lexems;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( name == null ) {
			sb.append("<noname>");
		} else {
			sb.append(name);
		}
		if( type != null ) {
			sb.append(" (");
			sb.append(type);
			sb.append(")");
		}
		sb.append(" [");
		sb.append("term=");
		sb.append(isTerm);
		sb.append(", defined=");
		sb.append(isDefined);
		sb.append("]");
		return sb.toString();
	}
}
