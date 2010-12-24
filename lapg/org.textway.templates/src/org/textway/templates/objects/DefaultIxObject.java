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

public abstract class DefaultIxObject implements IxObject, IxAdaptable {

	public Object castTo(String qualifiedName) throws EvaluationException {
		throw new EvaluationException("cast to '" + qualifiedName + "' is undefined for `" + getType() + "`");
	}

	public String asString() throws EvaluationException {
		throw new EvaluationException("string representation is undefined for `" + getType() + "`");
	}

	public boolean asBoolean() {
		return true;
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
		throw new EvaluationException("'is' (instanceof) operator is undefined for `" + getType() + "`");
	}

	public Iterator asSequence() throws EvaluationException {
		throw new EvaluationException("iteration is not supported over `" + getType() + "`");
	}

	protected abstract String getType();
}
