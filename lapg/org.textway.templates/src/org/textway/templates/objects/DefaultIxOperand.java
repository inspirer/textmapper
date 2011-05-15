/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.templates.objects;

import org.textway.templates.api.EvaluationException;

public class DefaultIxOperand implements IxOperand, IxWrapper {

	private final Object myObject;

	public DefaultIxOperand(Object object) {
		this.myObject = object;
	}

	public Object plus(Object v) throws EvaluationException {
		throw new EvaluationException("'+' is not supported for `" + getType() + "`");
	}

	public Object minus(Object v) throws EvaluationException {
		throw new EvaluationException("'-' is not supported for `" + getType() + "`");
	}

	public Object multiply(Object v) throws EvaluationException {
		throw new EvaluationException("'*' is not supported for `" + getType() + "`");
	}

	public Object div(Object v) throws EvaluationException {
		throw new EvaluationException("'/' is not supported for `" + getType() + "`");
	}

	public Object mod(Object v) throws EvaluationException {
		throw new EvaluationException("'%' is not supported for `" + getType() + "`");
	}

	public Object or(Object v) throws EvaluationException {
		throw new EvaluationException("'|' is not supported for `" + getType() + "`");
	}

	public Object and(Object v) throws EvaluationException {
		throw new EvaluationException("'&' is not supported for `" + getType() + "`");
	}

	public Object xor(Object v) throws EvaluationException {
		throw new EvaluationException("'^' is not supported for `" + getType() + "`");
	}

	public Object next() throws EvaluationException {
		throw new EvaluationException("increment is not supported for `" + getType() + "`");
	}

	public Object previous() throws EvaluationException {
		throw new EvaluationException("decrement is not supported for `" + getType() + "`");
	}

	public Object negative() throws EvaluationException {
		throw new EvaluationException("negative is not supported for `" + getType() + "`");
	}

	public Object leftShift(Object v) throws EvaluationException {
		throw new EvaluationException("left shift is not supported for `" + getType() + "`");
	}

	public Object rightShift(Object v) throws EvaluationException {
		throw new EvaluationException("right shift is not supported for `" + getType() + "`");
	}

	public int compareTo(Object v) throws EvaluationException {
		throw new EvaluationException("compare is not supported for `" + getType() + "`");
	}

	public boolean equalsTo(Object v) throws EvaluationException {
		Object real = v instanceof IxWrapper ? ((IxWrapper) v).getObject() : v;
		return myObject == null ? real == null : myObject.equals(real);
	}

	protected String getType() {
		return "Object";
	}

	public Object getObject() {
		return myObject;
	}
}
