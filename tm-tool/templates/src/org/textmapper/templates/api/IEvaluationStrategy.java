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
package org.textmapper.templates.api;

import org.textmapper.templates.api.types.ITypesRegistry;
import org.textmapper.templates.ast.ExpressionNode;
import org.textmapper.templates.bundle.IBundleEntity;
import org.textmapper.templates.objects.IxFactory;
import org.textmapper.templates.storage.Resource;

/**
 * Defines environment for evaluating set of templates.
 */
public interface IEvaluationStrategy extends TemplatesStatus, IStreamHandler, IxFactory {

	Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException;

	String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, SourceElement caller);

	Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, SourceElement caller) throws EvaluationException;

	IBundleEntity loadEntity(String qualifiedName, int kind, SourceElement referer);

	String eval(Resource resource, EvaluationContext context);

	String toString(Object o, ExpressionNode referer) throws EvaluationException;

	String getTitle(Object object);

	ITypesRegistry getTypesRegistry();

	IEvaluationCache getCache();
}