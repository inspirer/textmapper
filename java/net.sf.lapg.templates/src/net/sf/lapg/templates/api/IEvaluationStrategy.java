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
package net.sf.lapg.templates.api;

import java.util.Iterator;

import net.sf.lapg.templates.ast.ExpressionNode;

/**
 * Defines environment for evaluating set of templates.
 */
public interface IEvaluationStrategy extends INavigationStrategy<Object>, IProblemCollector, IStreamHandler {

	Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException;

	String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, ILocatedEntity referer);

	Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) throws EvaluationException;

	IBundleEntity loadEntity(String qualifiedName, int kind, ILocatedEntity referer);

	String eval(ILocatedEntity referer, String template, String templateId, EvaluationContext context, int line);

	boolean toBoolean(Object o) throws EvaluationException;

	Iterator<?> getCollectionIterator(Object o);

	String toString(Object o, ExpressionNode referer) throws EvaluationException;

	String getTitle(Object object);

	IEvaluationCache getCache();
}