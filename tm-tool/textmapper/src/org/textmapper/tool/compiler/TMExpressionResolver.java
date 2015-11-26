/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.compiler;

import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IType;
import org.textmapper.templates.types.TiExpressionBuilder;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.templates.types.TypesUtil;
import org.textmapper.tool.parser.ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * evgeny, 1/29/13
 */
public class TMExpressionResolver {

	private final TMResolver resolver;
	private final TypesRegistry types;
	private final String myTypesPackage;

	public TMExpressionResolver(TMResolver resolver, TypesRegistry types, String typesPackage) {
		this.resolver = resolver;
		this.types = types;
		this.myTypesPackage = typesPackage;
	}

	public String getTypesPackage() {
		return myTypesPackage;
	}

	@SuppressWarnings("unchecked")
	Map<String, Object> convert(TmaAnnotations astAnnotations, String kind) {
		if (astAnnotations == null || astAnnotations.getAnnotations() == null) {
			return null;
		}
		return convert(astAnnotations.getAnnotations(), astAnnotations, kind);
	}

	private Map<String, Object> convert(List<TmaAnnotation> list, TmaNode anchor, String kind) {
		// Load class
		IClass annoClass = types.getClass(myTypesPackage + "." + kind, null);
		if (annoClass == null) {
			error(anchor, "cannot load class `" + myTypesPackage + "." + kind + "`");
			return null;
		}

		Map<String, Object> result = new HashMap<String, Object>();
		for (TmaAnnotation entry : list) {
			if (entry.getSyntaxProblem() != null) {
				continue;
			}
			String name = entry.getName();
			IFeature feature = annoClass.getFeature(name);
			if (feature == null) {
				error(entry, "unknown annotation `" + name + "`");
				continue;
			}

			IType expected = feature.getType();

			ITmaExpression expr = entry.getExpression();
			if (expr == null) {
				if (!TypesUtil.isBooleanType(expected)) {
					error(entry, "expected value of type `" + expected.toString() +
							"` instead of boolean");
					continue;
				}
				result.put(name, Boolean.TRUE);
			} else {
				result.put(name, convertExpression(expr, expected));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	Object convertExpression(ITmaExpression expression, IType type) {
		return new TiExpressionBuilder<ITmaExpression>() {
			@Override
			public IClass resolveType(String className) {
				return types.getClass(className, null);
			}

			@Override
			public Object resolve(ITmaExpression expression, IType type) {
				if (expression instanceof TmaInstance) {
					List<TmaMapEntry> list = ((TmaInstance) expression).getEntries();
					Map<String, ITmaExpression> props = new HashMap<String, ITmaExpression>();
					if (list != null) {
						for (TmaMapEntry entry : list) {
							props.put(entry.getName(), entry.getValue());
						}
					}
					String name = ((TmaInstance) expression).getClassName().getQualifiedId();
					if (name.indexOf('.') < 0) {
						name = myTypesPackage + "." + name;
					}
					return convertNew(expression, name, props, type);
				}
				if (expression instanceof TmaArray) {
					List<ITmaExpression> list = ((TmaArray) expression).getContent();
					return convertArray(expression, list, type);
				}
				if (expression instanceof TmaSymref) {
					IClass symbolClass = types.getClass("common.Symbol", null);
					if (symbolClass == null) {
						report(expression, "cannot load class `common.Symbol`");
						return null;
					}
					if (!symbolClass.isSubtypeOf(type)) {
						report(expression, "`" + symbolClass.toString() + "` is not a subtype of " +
								"`" + type.toString() + "`");
						return null;
					}
					return resolver.resolve((TmaSymref) expression);
				}
				if (expression instanceof TmaLiteral) {
					Object value = ((TmaLiteral) expression).getValue();
					return convertLiteral(expression, value, type);
				}
				return null;
			}

			@Override
			public void report(ITmaExpression expression, String message) {
				error(expression, message);
			}
		}.resolve(expression, type);
	}

	private void error(ITmaNode n, String message) {
		resolver.error(n, message);
	}
}
