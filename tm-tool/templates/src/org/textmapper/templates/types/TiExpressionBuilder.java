/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

import org.textmapper.templates.api.types.*;
import org.textmapper.templates.api.types.IDataType.Constraint;
import org.textmapper.templates.api.types.IDataType.DataTypeKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class TiExpressionBuilder<Node> {

	public abstract IClass resolveType(String className);

	public abstract Object resolve(Node expression, IType type);

	public abstract void report(Node expression, String message);

	protected Object convertLiteral(Node node, Object literal, IType type) {
		if (!(type instanceof IDataType)) {
			report(node, "expected value of type `" + type.toString() + "` instead of literal");
			return null;
		}
		IDataType dataType = (IDataType) type;
		DataTypeKind kind;
		if (literal instanceof Boolean) {
			kind = DataTypeKind.BOOL;
		} else if (literal instanceof Number) {
			kind = DataTypeKind.INT;
		} else {
			if(!(literal instanceof String)) {
				literal = literal.toString();
			}
			kind = DataTypeKind.STRING;
		}

		if (kind != dataType.getKind()) {
			String kindValue = kind == DataTypeKind.BOOL ? "bool" : kind == DataTypeKind.INT ? "int" : "string";
			report(node, "expected value of type `" + dataType.toString() + "` instead of `" + kindValue + "`");
			return null;
		}

		if (kind == DataTypeKind.STRING) {
			String s = (String) literal;
			for (Constraint constraint : dataType.getConstraints()) {
				String message = ConstraintUtil.validate(s, constraint);
				if (message != null) {
					report(node, message);
					return null;
				}
			}
		}

		return literal;
	}

	protected Object convertNew(Node node, String className, Map<String, Node> properties, IType type) {
		IClass aClass = resolveType(className);
		if (aClass == null) {
			report(node, "cannot instantiate `" + className + "`: class not found");
			return null;
		}

		if (type != null && !aClass.isSubtypeOf(type)) {
			report(node, "`" + aClass.toString() + "` is not a subtype of `" + type.toString() + "`");
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		if(properties != null) {
			for (Entry<String, Node> item : properties.entrySet()) {
				String key = item.getKey();

				IType requiredType = null;
				{
					IFeature feature = aClass.getFeature(key);
					if(feature != null) {
						requiredType = feature.getType();
					} else {
						IMethod method = aClass.getMethod(key);
						if(method != null) {
							IType[] parameterTypes = method.getParameterTypes();
							IType[] requiredParams = new IType[1 + (parameterTypes != null ? parameterTypes.length : 0)];
							requiredParams[0] = method.getDeclaringClass();
							if(parameterTypes != null) {
								System.arraycopy(parameterTypes, 0, requiredParams, 1, parameterTypes.length);
							}
							requiredType = new TiClosureType(requiredParams);
						}
					}
				}

				if (requiredType == null) {
					report(node, "trying to initialize unknown feature/method `" + key + "` in class `" + className + "`");
					continue;
				}

				Object value = resolve(item.getValue(), requiredType);
				if (value != null) {
					result.put(key, value);
				}
			}
		}

		return new TiInstance(aClass, result);
	}

	protected Object convertArray(Node node, List<Node> array, IType type) {
		if (!(type instanceof IArrayType)) {
			report(node, "expected value of type `" + type.toString() + "` instead of array");
			return null;
		}

		if(array == null) {
			return new ArrayList();
		}

		IType innerType = ((IArrayType) type).getInnerType();
		List<Object> result = new ArrayList<Object>(array.size());
		for (Node expression : array) {
			Object subexpr = resolve(expression, innerType);
			if (subexpr != null) {
				result.add(subexpr);
			}
		}

		return result;
	}

	protected Object convertClosure(Node node, TiClosure closure, IType type) {
		if (!(type instanceof IClosureType)) {
			report(node, "expected value of type `" + type.toString() + "` instead of closure");
			return null;
		}

		if(!closure.matches((IClosureType) type)) {
			report(node, "expected closure of type `" + type.toString() + "`");
			return null;
		}

		return closure;
	}
}
