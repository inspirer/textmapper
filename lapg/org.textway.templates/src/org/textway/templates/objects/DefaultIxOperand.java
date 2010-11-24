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
package org.textway.templates.objects;

import org.textway.templates.api.EvaluationException;

public class DefaultIxOperand implements IxOperand, IxWrapper {

	private final Object myObject;

	public DefaultIxOperand(Object object) {
		this.myObject = object;
	}

	public Object plus(Object v) throws EvaluationException {
		throw new EvaluationException("cannot add to an object");
	}

	public Object minus(Object v) throws EvaluationException {
		throw new EvaluationException("cannot subtract from an object");
	}

	public Object multiply(Object v) throws EvaluationException {
		throw new EvaluationException("cannot multiply an object");
	}

	public Object div(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide an object");
	}

	public Object mod(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide an object");
	}

	public Object or(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to an object");
	}

	public Object and(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to an object");
	}

	public Object xor(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to an object");
	}

	public Object next() throws EvaluationException {
		throw new EvaluationException("cannot increment an object");
	}

	public Object previous() throws EvaluationException {
		throw new EvaluationException("cannot decrement an object");
	}

	public Object negative() throws EvaluationException {
		throw new EvaluationException("cannot make an object negative");
	}

	public Object leftShift(Object v) throws EvaluationException {
		throw new EvaluationException("cannot shift an object");
	}

	public Object rightShift(Object v) throws EvaluationException {
		throw new EvaluationException("cannot shift an object");
	}

	public int compareTo(Object v) throws EvaluationException {
		throw new EvaluationException("cannot compare objects");
	}

	public boolean equalsTo(Object v) throws EvaluationException {
		Object real = v instanceof IxWrapper ? ((IxWrapper) v).getObject() : v;
		return myObject == null ? real == null : myObject.equals(real);
	}

	public Object getObject() {
		return myObject;
	}
}
