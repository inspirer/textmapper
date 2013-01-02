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
package org.textmapper.templates.objects;

import org.textmapper.templates.api.EvaluationException;

import java.util.Iterator;

public class JavaStringIxObject implements IxWrapper, IxOperand, IxAdaptable {

	private final String myString;

	public JavaStringIxObject(String string) {
		this.myString = string;
	}

	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cannot cast a string");
	}

	public String asString() throws EvaluationException {
		return myString != null ? myString : "null";
	}

	public boolean asBoolean() {
		return myString != null && myString.length() > 0;
	}

	public Iterator asSequence() throws EvaluationException {
		throw new EvaluationException("cannot iterate over a string");
	}

	public Object plus(Object v) throws EvaluationException {
		return asString() + asString(v);
	}

	public Object minus(Object v) throws EvaluationException {
		throw new EvaluationException("cannot subtract from a string");
	}

	public Object multiply(Object v) throws EvaluationException {
		throw new EvaluationException("cannot multiply a string");
	}

	public Object div(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide a string");
	}

	public Object mod(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide a string");
	}

	public Object or(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to a string");
	}

	public Object and(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to a string");
	}

	public Object xor(Object v) throws EvaluationException {
		throw new EvaluationException("cannot apply the bitwise operation to a string");
	}

	public Object next() throws EvaluationException {
		throw new EvaluationException("cannot increment a string");
	}

	public Object previous() throws EvaluationException {
		throw new EvaluationException("cannot decrement a string");
	}

	public Object negative() throws EvaluationException {
		throw new EvaluationException("cannot make a string negative");
	}

	public Object leftShift(Object v) throws EvaluationException {
		throw new EvaluationException("cannot shift a string");
	}

	public Object rightShift(Object v) throws EvaluationException {
		throw new EvaluationException("cannot shift a string");
	}

	public int compareTo(Object v) throws EvaluationException {
		return myString.compareTo(asString(v));
	}

	public boolean equalsTo(Object v) throws EvaluationException {
		return myString.equals(asString(v)); 
	}

	private String asString(Object v) throws EvaluationException {
		if(v instanceof IxAdaptable) {
			return ((IxAdaptable) v).asString();
		}
		Object real = v instanceof IxWrapper ? ((IxWrapper) v).getObject() : v;
		if(real instanceof String) {
			return (String) real;
		}
		if(real == null) {
			return "null";
		}
		// TODO use factory
		return real.toString();
	}

	public Object getObject() {
		return myString;
	}
}
