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
package org.textway.templates.eval;

import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.api.IInstanceObject;
import org.textway.templates.api.IProxyObject;
import org.textway.templates.api.types.IArrayType;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;
import org.textway.templates.api.types.IType;
import org.textway.templates.types.TiArrayType;
import org.textway.templates.types.TypesUtil;

public class ObjectWithType implements IProxyObject, IInstanceObject {

	private final Object rawObject;
	private final IEvaluationStrategy evaluationStrategy;
	private final IType type;

	public ObjectWithType(Object rawObject, IEvaluationStrategy evaluationStrategy, IType type) {
		this.rawObject = rawObject;
		this.evaluationStrategy = evaluationStrategy;
		this.type = type;
	}

	public Object getProperty(String propertyName) throws EvaluationException {
		if(type instanceof IClass) {
			IFeature feature = ((IClass) type).getFeature(propertyName);
			if(feature == null) {
				throw new EvaluationException("Property `" + propertyName + "` is absent in class " + ((IClass) type).getQualifiedName());
			}

			Object result = evaluationStrategy.getProperty(rawObject, propertyName);
			if(result == null) {
				return null;
			}

			IType resultType = TypesUtil.getFeatureType(feature);
			return new ObjectWithType(result, evaluationStrategy, resultType);
		}
		
		return evaluationStrategy.getProperty(rawObject, propertyName);
	}

	public Object callMethod(String methodName, Object[] args) throws EvaluationException {
		return evaluationStrategy.callMethod(rawObject, methodName, args);
	}

	public Object getByIndex(Object index) throws EvaluationException {
		if(type instanceof IArrayType) {
			Object result = evaluationStrategy.getByIndex(rawObject, index);
			if(result == null) {
				return null;
			}

			IType resultType = ((IArrayType)type).getInnerType();
			return new ObjectWithType(result, evaluationStrategy, resultType);
		}

		return evaluationStrategy.getByIndex(rawObject, index);
	}

	public IType getType() {
		return type;
	}
}
