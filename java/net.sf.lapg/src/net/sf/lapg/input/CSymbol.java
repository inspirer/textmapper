package net.sf.lapg.input;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.input.Parser.ParseException;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CSymbol implements Symbol, ILocatedEntity {

	private final String name;

	private int line;
	int index;
	private String type;
	private boolean isTerm = false, isDefined = false;

	public CSymbol(String name, int line) {
		this.line = line;
		this.name = name;
		this.index = -1;
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

	void setTerminal( String type, boolean hasRegExp, int line ) throws ParseException {
		if( name.equals(CSyntax.INPUT) ) {
			throw new ParseException("cannot declare terminal with name `"+name+"` (reserved non-terminal) at line " + line);
		}
		if( name.equals(CSyntax.ERROR) && hasRegExp ) {
			throw new ParseException("cannot have regexp for symbol with name `"+name+"` (reserved non-terminal) at line " + line);
		}
		if( name.endsWith(CSyntax.OPTSUFFIX) ) {
			throw new ParseException("cannot declare terminal with name `"+name+"` ("+CSyntax.OPTSUFFIX+" suffix is reserved for non-terms) at line " + line);
		}
		setDefined(type, line, true);
	}

 	void setNonTerminal( String type, int line ) throws ParseException {
		if( name.endsWith(CSyntax.OPTSUFFIX) && line != 0 ) {
			throw new ParseException("cannot declare non-terminal with name `"+name+"` ("+CSyntax.OPTSUFFIX+" suffix symbols are generated automatically) at line " + line);
		}
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

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
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
