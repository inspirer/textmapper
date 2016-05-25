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
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsList;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.lapg.util.RhsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 1/3/13
 */
class LiRhsList extends LiRhsRoot implements RhsList {

	private final LiRhsSequence element;
	private final LiRhsPart separator;
	private final boolean nonEmpty;
	private final LiRhsSequence customInitialElement;
	private final boolean rightRecursive;
	private RhsSequence[] preprocessed;

	LiRhsList(LiRhsSequence element, LiRhsPart separator, boolean nonEmpty,
			  LiRhsSequence customInitialElement, boolean rightRecursive,
			  boolean isRewrite, SourceElement origin) {
		super(null, origin);
		if (element == null) {
			throw new NullPointerException();
		}
		if (separator != null && !nonEmpty) {
			throw new IllegalArgumentException("lists with separator should have at least one element");
		}
		if (customInitialElement != null && !nonEmpty) {
			throw new IllegalArgumentException("custom initial element is allowed only in non-empty lists");
		}
		this.element = element;
		this.separator = separator;
		this.nonEmpty = nonEmpty;
		this.customInitialElement = customInitialElement;
		this.rightRecursive = rightRecursive;
		register(isRewrite, element, separator, customInitialElement);
	}

	@Override
	public LiRhsSequence getElement() {
		return element;
	}

	@Override
	public LiRhsPart getSeparator() {
		return separator;
	}

	@Override
	public boolean isNonEmpty() {
		return nonEmpty;
	}

	@Override
	public boolean isRightRecursive() {
		return rightRecursive;
	}

	@Override
	public LiRhsSequence getCustomInitialElement() {
		return customInitialElement;
	}

	@Override
	List<RhsCFPart[]> expand(ExpansionContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRhsList that = (LiRhsList) o;

		if (separator != null ? !separator.structurallyEquals(that.separator) : that.separator != null) return false;
		if (customInitialElement != null ? !customInitialElement.structurallyEquals(that.customInitialElement) :
				that.customInitialElement != null)
			return false;
		if (nonEmpty != that.nonEmpty) return false;
		if (rightRecursive != that.rightRecursive) return false;
		return element.structurallyEquals(that.element);
	}

	@Override
	public int structuralHashCode() {
		int result = element.structuralHashCode();
		result = 31 * result + (separator != null ? separator.structuralHashCode() : 0);
		result = 31 * result + (customInitialElement != null ? customInitialElement.structuralHashCode() : 0);
		result = 31 * result + (nonEmpty ? 1 : 0);
		result = 31 * result + (rightRecursive ? 1 : 0);
		return result;
	}

	@Override
	public Kind getKind() {
		return Kind.List;
	}

	@Override
	protected void toString(StringBuilder sb) {
		if (customInitialElement != null) {
			boolean nonEmptyInitial = customInitialElement.getParts().length != 0;
			if (nonEmptyInitial) {
				sb.append("(");
			}
			if (rightRecursive) {
				sb.append("(");
				element.toString(sb);
				if (separator != null) {
					sb.append(" ");
					separator.toString(sb);
				}
				sb.append(" /rr)*");
				if (nonEmptyInitial) {
					sb.append(' ');
					customInitialElement.toString(sb);
				}
			} else {
				if (nonEmptyInitial) {
					customInitialElement.toString(sb);
					sb.append(" ");
				}
				sb.append("(");
				if (separator != null) {
					separator.toString(sb);
					sb.append(" ");
				}
				element.toString(sb);
				sb.append(")*");
			}
			if (nonEmptyInitial) {
				sb.append(")");
			}
		} else {
			sb.append("(");
			element.toString(sb);
			if (separator != null) {
				sb.append(" separator ");
				separator.toString(sb);
			}
			if (rightRecursive) {
				sb.append(" /rr");
			}
			sb.append(")").append(nonEmpty ? "+" : "*");
		}
	}

	@Override
	public RhsSequence[] asRules() {
		return preprocess();
	}

	@Override
	protected RhsSequence[] preprocess() {
		if (preprocessed != null) return preprocessed;

		LiRhsSymbol selfRef = new LiRhsSymbol(getLeft(), null, true, this);
		List<LiRhsPart> listRule = new ArrayList<>(3);
		listRule.add(rightRecursive ? element : selfRef);
		if (separator != null) {
			listRule.add(separator);
		}
		listRule.add(rightRecursive ? selfRef : element);
		LiRhsSequence rule1 = new LiRhsSequence(null, listRule.toArray(new LiRhsPart[listRule.size()]), true, this);

		LiRhsSequence rule2;
		if (nonEmpty) {
			rule2 = customInitialElement != null ? customInitialElement : element;
		} else {
			rule2 = new LiRhsSequence(null, new LiRhsPart[0], false, this);
		}
		register(true, rule1, rule2, customInitialElement, element, separator);
		return preprocessed = new RhsSequence[]{rule1, rule2};
	}

	@Override
	public String getProvisionalName() {
		StringBuilder sb = new StringBuilder();
		Symbol representative = RhsUtil.getRepresentative(element);
		if (representative != null) {
			sb.append(LiUtil.getSymbolName(representative));
			sb.append(nonEmpty || separator != null ? "_list" : "_optlist");
		} else {
			RhsSymbol[] rhsSymbols = RhsUtil.getRhsSymbols(element);
			sb.append("list_of_");
			if (rhsSymbols.length > 0) {
				sb.append(LiUtil.getSymbolName(rhsSymbols[0]));
				if (rhsSymbols.length > 1) {
					sb.append("_and_").append(rhsSymbols.length - 1).append("_elements");
				}
			} else {
				sb.append("unknown");
			}
		}
		if (separator != null) {
			Symbol separatorTerminal = RhsUtil.getRepresentative(separator);
			if (separatorTerminal instanceof Terminal && ((Terminal) separatorTerminal).isConstant()) {
				String val = ((Terminal) separatorTerminal).getConstantValue();
				sb.append("_").append(FormatUtil.toIdentifier(val)).append("_separated");
			} else {
				sb.append("_withsep");
			}
		}
		if (rightRecursive) {
			sb.append("_rr");
		}
		return sb.toString();
	}
}
