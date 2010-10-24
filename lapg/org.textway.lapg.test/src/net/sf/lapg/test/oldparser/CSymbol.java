/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.lapg.test.oldparser;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.test.oldparser.LapgParser.ParseException;

public class CSymbol implements Symbol, ILocatedEntity {

	private final String name;

	private String input;
	private int line;

	int index;
	private String type;
	private boolean isTerm = false, isDefined = false;

	public CSymbol(String name, String input, int line) {
		this.name = name;
		this.index = -1;
		this.input = input;
		this.line = line;
	}

	private void setDefined(String ntype, boolean nisterm, String ninput, int nline) throws ParseException {
		if (isDefined) {
			if (nisterm != isTerm) {
				throw new ParseException((isTerm ? "redeclaring terminal `" + name + "` as non-terminal"
						: "redeclaring non-terminal `" + name + "` as terminal")
						+ " at line " + nline + " (previously declared at " + this.line + ")");
			}
			if (this.type != null && ntype != null && !this.type.equals(ntype)) {
				throw new ParseException("redeclaring type for `" + name + "` at line " + nline
						+ " (previously declared at line " + this.line + ")");
			}
		} else {
			if (line != 0) {
				input = ninput;
				line = nline;
			}
			isDefined = true;
			isTerm = nisterm;
		}
		if (type == null) {
			type = ntype;
		}
	}

	void setTerminal(String type, boolean hasRegExp, String input, int line) throws ParseException {
		if (name.equals(CSyntax.INPUT)) {
			throw new ParseException("cannot declare terminal with name `" + name
					+ "` (reserved non-terminal) at line " + line);
		}
		if (name.equals(CSyntax.ERROR) && hasRegExp) {
			throw new ParseException("cannot have regexp for symbol with name `" + name
					+ "` (reserved non-terminal) at line " + line);
		}
		if (name.endsWith(CSyntax.OPTSUFFIX)) {
			throw new ParseException("cannot declare terminal with name `" + name + "` (" + CSyntax.OPTSUFFIX
					+ " suffix is reserved for non-terms) at line " + line);
		}
		setDefined(type, true, input, line);
	}

	void setNonTerminal(String type, String input, int line) throws ParseException {
		if (name.endsWith(CSyntax.OPTSUFFIX) && line != 0) {
			throw new ParseException("cannot declare non-terminal with name `" + name + "` (" + CSyntax.OPTSUFFIX
					+ " suffix symbols are generated automatically) at line " + line);
		}
		setDefined(type, false, input, line);
	}

	void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return input + "," + line;
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

    public String getId() {
        return name;
    }

    public String getType() {
		return type;
	}

	public void addAnnotation(String name, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object getAnnotation(String name) {
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (name == null) {
			sb.append("<noname>");
		} else {
			sb.append(name);
		}
		if (type != null) {
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

	public int getEndOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getResourceName() {
		return input;
	}
}
