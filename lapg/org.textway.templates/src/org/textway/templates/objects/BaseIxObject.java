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

import java.util.Iterator;

public abstract class BaseIxObject implements IxWrapper, IxObject, IxAdaptable, IxOperand {

	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cast to '" + qualifiedName + "' is undefined for `" + getType() + "`");
	}

	public String asString() throws EvaluationException {
		throw new EvaluationException("string representation is undefined for `" + getType() + "`");
	}

	public boolean asBoolean() {
		return false;
	}

	public Object getProperty(String propertyName) throws EvaluationException {
		throw new EvaluationException("property `" + propertyName + "` is absent in `" + getType() + "`");
	}

	public Object callMethod(String methodName, Object[] args) throws EvaluationException {
		throw new EvaluationException("method `" + methodName + "` is absent in `" + getType() + "`");
	}

	public Object getByIndex(Object index) throws EvaluationException {
		throw new EvaluationException("index operator is not supported for `" + getType() + "`");
	}

	public boolean is(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("'is' (instanceof) operator is not for `" + getType() + "`");
	}

	public Iterator asSequence() throws EvaluationException {
		throw new EvaluationException("iteration is not supported over `" + getType() + "`");
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
		throw new EvaluationException("equals is not supported for `" + getType() + "`");
	}

	protected abstract String getType();
}
