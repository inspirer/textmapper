/**
 * Copyright 2002-2019 Evgeny Gryaznov
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

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.rule.RhsPredicate;

import java.util.Arrays;

class LiRhsPredicate implements RhsPredicate, DerivedSourceElement {

	private final Operation operation;
	private final LiRhsPredicate[] children;
	private final TemplateParameter param;
	private final Object value;
	private final SourceElement origin;

	public LiRhsPredicate(Operation operation, LiRhsPredicate[] children, TemplateParameter param,
						  Object value, SourceElement origin) {
		this.operation = operation;
		this.children = children;
		this.param = param;
		this.value = value;
		this.origin = origin;
	}

	@Override
	public Operation getOperation() {
		return operation;
	}

	@Override
	public LiRhsPredicate[] getChildren() {
		return children;
	}

	@Override
	public TemplateParameter getParameter() {
		return param;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}

	@Override
	public boolean apply(TemplateEnvironment env) {
		switch (operation) {
			case And:
				for (LiRhsPredicate child : children) {
					if (!child.apply(env)) return false;
				}
				return true;
			case Or:
				for (LiRhsPredicate child : children) {
					if (child.apply(env)) return true;
				}
				return false;
			case Equals:
				return value.equals(env.getValue(param));
			case Not:
				return !children[0].apply(env);
		}
		throw new IllegalStateException();
	}

	public void toString(StringBuilder sb) {
		switch (operation) {
			case And:
			case Or: {
				boolean first = true;
				for (LiRhsPredicate child : children) {
					if (first) {
						first = false;
					} else {
						sb.append(operation == Operation.And ? " && " : " || ");
					}
					boolean needParentheses = (child.getOperation() == Operation.Or);
					if (needParentheses) sb.append("(");
					child.toString(sb);
					if (needParentheses) sb.append(")");
				}
				break;
			}
			case Equals:
				sb.append(param.getNameText());
				sb.append(" == ");
				sb.append(value);
				break;
			case Not:
				sb.append("!(");
				children[0].toString(sb);
				sb.append(")");
				break;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsPredicate that = (LiRhsPredicate) o;

		if (!Arrays.equals(children, that.children)) return false;
		if (operation != that.operation) return false;
		if (param != null ? !param.equals(that.param) : that.param != null) return false;
		if (value != null ? !value.equals(that.value) : that.value != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = operation.hashCode();
		result = 31 * result + (children != null ? Arrays.hashCode(children) : 0);
		result = 31 * result + (param != null ? param.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
