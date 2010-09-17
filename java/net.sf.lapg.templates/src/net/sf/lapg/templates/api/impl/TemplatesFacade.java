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
package net.sf.lapg.templates.api.impl;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.INavigationStrategy.Factory;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.ITemplate;

public class TemplatesFacade {

	private IEvaluationStrategy evaluationStrategy;

	private final INavigationStrategy.Factory factory;
	private final TemplatesRegistry registry;
	private final IProblemCollector collector;

	public TemplatesFacade(Factory factory, TemplatesRegistry registry, IProblemCollector collector) {
		this.factory = factory;
		this.registry = registry;
		this.collector = collector;
	}

	private IEvaluationStrategy getEvaluationStrategy() {
		if(evaluationStrategy == null) {
			evaluationStrategy = createEvaluationStrategy(factory, registry);
		}
		return evaluationStrategy;
	}

	protected IEvaluationStrategy createEvaluationStrategy(INavigationStrategy.Factory factory, TemplatesRegistry registry) {
		return new DefaultEvaluationStrategy(this, factory, registry);
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		ITemplate t = (ITemplate) getEvaluationStrategy().loadEntity(name, IBundleEntity.KIND_TEMPLATE, referer);
		return getEvaluationStrategy().evaluate(t, context, arguments, referer);
	}

	public final void fireError(ILocatedEntity referer, String error) {
		collector.fireError(referer, error);
	}

	public void createFile(String name, String contents) {
		// ignore
	}
}
