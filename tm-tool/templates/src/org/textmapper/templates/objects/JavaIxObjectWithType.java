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
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IType;

public class JavaIxObjectWithType extends DefaultJavaIxObject {

	private final IClass type;

	public JavaIxObjectWithType(Object wrapped, IClass type) {
		super(wrapped);
		this.type = type;
	}

	public Object getProperty(SourceElement caller, String propertyName) throws EvaluationException {
		if(type == null) {
			return super.getProperty(caller, propertyName);
		}

		IFeature feature = type.getFeature(propertyName);
		if (feature == null) {
			throw new EvaluationException("Property `" + propertyName + "` is absent in class " + type.getQualifiedName());
		}

		Object result = super.getProperty(caller, propertyName);
		if (result == null || result instanceof IxWrapper) {
			return result;
		}

		IType resultType = feature.getType();
		if(resultType instanceof IClass) {
			return new JavaIxObjectWithType(result, (IClass) resultType);
		}

		return result;
	}
}
