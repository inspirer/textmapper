/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.*;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 1/3/13
 */
public class LiRhsList extends LiRhsRoot implements RhsList {

	private final LiRhsPart element;
	private final LiRhsPart separator;
	private final boolean nonEmpty;
	private final LiRhsPart customInitialElement;
	private final boolean rightRecursive;

	public LiRhsList(LiRhsPart element, LiRhsPart separator, boolean nonEmpty,
					 LiRhsPart customInitialElement, boolean rightRecursive,
					 SourceElement origin) {
		super(null, origin);
		if (element == null) {
			throw new NullPointerException();
		}
		if (separator != null && !nonEmpty) {
			throw new IllegalArgumentException("list with separator should have at least one element");
		}
		if (customInitialElement != null && !nonEmpty) {
			throw new IllegalArgumentException("custom initial element is allowed only in non-empty lists");
		}
		this.element = element;
		this.separator = separator;
		this.nonEmpty = nonEmpty;
		this.customInitialElement = customInitialElement;
		this.rightRecursive = rightRecursive;
		register(element, separator, customInitialElement);
	}

	@Override
	public RhsPart getElement() {
		return element;
	}

	@Override
	public RhsPart getSeparator() {
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
	public LiRhsPart getCustomInitialElement() {
		return customInitialElement;
	}

	@Override
	List<RhsSymbol[]> expand() {
		List<RhsSymbol[]> result = new ArrayList<RhsSymbol[]>();
		LiRhsSymbol selfRef = new LiRhsSymbol(getLeft(), null, this);
		if (nonEmpty) {
			result.addAll((customInitialElement != null ? customInitialElement : element).expand());
		} else {
			result.add(new RhsSymbol[0]);
		}
		List<LiRhsPart> listRule = new ArrayList<LiRhsPart>(3);
		listRule.add(rightRecursive ? element : selfRef);
		if (separator != null) {
			listRule.add(separator);
		}
		listRule.add(rightRecursive ? selfRef : element);

		result.addAll(LiRhsSequence.expandList(listRule.toArray(new LiRhsPart[listRule.size()])));
		return result;
	}

	@Override
	public boolean structuralEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LiRhsList that = (LiRhsList) o;

		if (separator != null ? !separator.structuralEquals(that.separator) : that.separator != null) return false;
		if (customInitialElement != null ? !customInitialElement.structuralEquals(that.customInitialElement) : that.customInitialElement != null)
			return false;
		if (nonEmpty != that.nonEmpty) return false;
		if (rightRecursive != that.rightRecursive) return false;
		return element.structuralEquals(that.element);
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
	public <T> T accept(RhsSwitch<T> switch_) {
		return switch_.caseList(this);
	}

	@Override
	public LiRhsList copy() {
		return new LiRhsList(
				element.copy(),
				separator == null ? null : separator.copy(),
				nonEmpty,
				customInitialElement == null ? null : customInitialElement.copy(),
				rightRecursive,
				getOrigin());
	}

	@Override
	protected void toString(StringBuilder sb) {
		if (customInitialElement != null) {
			boolean nonEmptyInitial = !(customInitialElement instanceof RhsSequence && ((RhsSequence) customInitialElement).getParts().length == 0);
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
}
