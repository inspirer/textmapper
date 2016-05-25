/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsSet;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LiRhsSet extends LiRhsPart implements RhsSet, TemplatedSymbolRef {

	private final Operation operation;
	private Symbol symbol;
	private LiRhsArgument[] args;
	private final LiRhsSet[] parts;

	public LiRhsSet(Operation operation, Symbol symbol, LiRhsArgument[] args, LiRhsSet[] parts, SourceElement origin) {
		super(origin);
		this.operation = operation;
		this.symbol = symbol;
		this.args = args;
		this.parts = parts;
	}

	@Override
	public Operation getOperation() {
		return operation;
	}

	@Override
	public Symbol getSymbol() {
		return symbol;
	}

	@Override
	public RhsArgument[] getArgs() {
		return args;
	}

	@Override
	public LiRhsSet[] getSets() {
		return parts;
	}

	@Override
	public String getProvisionalName() {
		StringBuilder sb = new StringBuilder();
		sb.append("setof_");
		toProvisionalName(sb);
		return sb.toString();
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		Terminal[] terminals = context.resolveSet(this);
		if (terminals == null) {
			// errors were already reported, so just ignoring the set
			return Collections.singletonList(RhsSymbol.EMPTY_LIST);
		}

		List<RhsCFPart[]> result = new ArrayList<>(terminals.length);
		for (Terminal t : terminals) {
			result.add(new RhsSymbol[]{new LiRhsSymbol(t, null, false, this)});
		}
		return result;
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsSet that = (LiRhsSet) o;
		if (operation != that.operation) return false;
		if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
		if (!Arrays.equals(args, that.args)) return false;
		return structurallyEquals(parts, that.parts);
	}

	@Override
	public int structuralHashCode() {
		int result = operation.hashCode();
		result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(args);
		result = 31 * result + structuralHashCode(parts);
		return result;
	}

	@Override
	protected void toString(StringBuilder sb) {
		sb.append("set(");
		toString(sb, 2);
		sb.append(")");
	}

	/**
	 * @param prec 0 - primary; 1 - intersection; 2 - union
	 */
	private void toString(StringBuilder sb, int prec) {
		int level = operation == Operation.Intersection ? 1 : operation == Operation.Union ? 2 : 0;
		if (level > prec) sb.append("(");

		boolean first = true;
		switch (operation) {
			case Any:
			case First:
			case Last:
			case Follow:
			case Precede:
				String text = toString(operation);
				sb.append(text);
				if (text.length() > 0) sb.append(" ");
				sb.append(symbol.getName());
				LiUtil.appendArguments(sb, args);
				break;
			case Complement:
				assert parts.length == 1;
				sb.append("~");
				parts[0].toString(sb, 0);
				break;
			case Intersection:
			case Union:
				for (LiRhsSet p : parts) {
					if (first) {
						first = false;
					} else {
						sb.append(operation == Operation.Intersection ? " & " : " | ");
					}
					p.toString(sb, operation == Operation.Intersection ? 0 : 1);
				}
				break;
		}
		if (level > prec) sb.append(")");
	}

	private static String toString(Operation op) {
		switch (op) {
			case Any:
				return "";
			case First:
				return "first";
			case Last:
				return "last";
			case Precede:
				return "precede";
			case Follow:
				return "follow";
		}
		throw new IllegalArgumentException();
	}

	private void toProvisionalName(StringBuilder sb) {
		boolean first = true;
		switch (operation) {
			case Any:
				sb.append(LiUtil.getSymbolName(symbol));
				break;
			case First:
			case Follow:
			case Last:
			case Precede:
				sb.append(toString(operation)).append("_").append(LiUtil.getSymbolName(symbol));
				break;
			case Complement:
				assert parts.length == 1;
				sb.append("not_");
				parts[0].toString(sb, 0);
				break;
			case Intersection:
			case Union:
				for (LiRhsSet p : parts) {
					if (first) {
						first = false;
					} else {
						sb.append(operation == Operation.Intersection ? "_" : "_or_");
					}
					p.toProvisionalName(sb);
				}
				break;
		}
	}

	@Override
	public Kind getKind() {
		return Kind.Set;
	}

	@Override
	public void setResolvedSymbol(Symbol symbol) {
		this.args = null;
		this.symbol = symbol;
	}
}
