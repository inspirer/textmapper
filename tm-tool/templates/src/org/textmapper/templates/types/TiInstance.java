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
package org.textmapper.templates.types;

import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.INamedEntity;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IMethod;
import org.textmapper.templates.objects.DefaultIxObject;

import java.util.Map;

public class TiInstance extends DefaultIxObject implements INamedEntity {

	private final IClass myClass;
	private final Map<String, Object> myValues;

	public TiInstance(IClass myClass, Map<String, Object> myValues) {
		this.myClass = myClass;
		this.myValues = myValues;
	}

	public String getType() {
		return myClass.getQualifiedName();
	}

	@Override
	public boolean is(String qualifiedName) throws EvaluationException {
		return myClass.isSubtypeOf(qualifiedName);
	}

	public Object getProperty(String propertyName) throws EvaluationException {
		IFeature feature = myClass.getFeature(propertyName);
		if (feature == null) {
			throw new EvaluationException("Property `" + propertyName + "` is absent in class " + myClass.getQualifiedName());
		}

		Object result = myValues.get(propertyName);
		if (result instanceof TiClosure) {
			TiClosure closure = (TiClosure) result;
			if(closure.getParametersCount() == 1) {
				return closure.callMethod("invoke", this);
			} else {
				return closure.getProperty("value");
			}
		}
		return result;
	}

	@Override
	public Object callMethod(String methodName, Object... args) throws EvaluationException {
		IMethod method = myClass.getMethod(methodName);
		if (method == null) {
			throw new EvaluationException("Method `" + methodName + "` is not declared in class " + myClass.getQualifiedName());
		}

		// FIXME check arguments count

		Object result = myValues.get(methodName);
		if (result instanceof TiClosure) {
			TiClosure closure = (TiClosure) result;
			Object[] args2 = new Object[args.length + 1];
			args2[0] = this;
			System.arraycopy(args, 0, args2, 1, args.length);
			return closure.callMethod("invoke", args2);
		}

		return super.callMethod(methodName, args);
	}

	@Override
	public String getTitle() {
		return "[" + myClass.getQualifiedName() + "]";
	}
}
