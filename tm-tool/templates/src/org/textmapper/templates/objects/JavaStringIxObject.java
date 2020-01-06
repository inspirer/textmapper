/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

	@Override
	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cannot cast a string");
	}

	@Override
	public String asString() throws EvaluationException {
		return myString != null ? myString : "null";
	}

	@Override
	public boolean asBoolean() {
		return myString != null && myString.length() > 0;
	}

	@Override
	public Iterator asSequence() throws EvaluationException {
		throw new EvaluationException("cannot iterate over a string");
	}

	@Override
	public Object plus(Object v) throws EvaluationException {
		return asString() + asString(v);
	}

	@Override
	public Object minus(Object v) throws EvaluationException {
		throw new EvaluationException("cannot subtract from a string");
	}

	@Override
	public Object multiply(Object v) throws EvaluationException {
		throw new EvaluationException("cannot multiply a string");
	}

	@Override
	public Object div(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide a string");
	}

	@Override
	public Object mod(Object v) throws EvaluationException {
		throw new EvaluationException("cannot divide a string");
	}

	@Override
	public int compareTo(Object v) throws EvaluationException {
		return myString.compareTo(asString(v));
	}

	@Override
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

	@Override
	public Object getObject() {
		return myString;
	}
}
