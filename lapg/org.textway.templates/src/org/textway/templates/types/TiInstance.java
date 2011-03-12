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
package org.textway.templates.types;

import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;
import org.textway.templates.objects.DefaultIxObject;

import java.util.Map;

public class TiInstance extends DefaultIxObject {

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
}
