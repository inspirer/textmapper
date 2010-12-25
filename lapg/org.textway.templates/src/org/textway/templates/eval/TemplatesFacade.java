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

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.ITemplate;
import org.textway.templates.bundle.IBundleEntity;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.objects.IxFactory;

public class TemplatesFacade {

	private IEvaluationStrategy evaluationStrategy;

	private final IxFactory factory;
	private final TemplatesRegistry registry;
	private final IProblemCollector collector;

	public TemplatesFacade(IxFactory factory, TemplatesRegistry registry) {
		this(factory, registry, registry.getCollector());
	}

	public TemplatesFacade(IxFactory factory, TemplatesRegistry registry, IProblemCollector collector) {
		this.factory = factory;
		this.registry = registry;
		this.collector = collector;
	}

	private IEvaluationStrategy getEvaluationStrategy() {
		if (evaluationStrategy == null) {
			evaluationStrategy = createEvaluationStrategy(factory, registry);
		}
		return evaluationStrategy;
	}

	protected IEvaluationStrategy createEvaluationStrategy(IxFactory factory, TemplatesRegistry registry) {
		return new DefaultEvaluationStrategy(this, factory, registry);
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		ITemplate t = (ITemplate) getEvaluationStrategy().loadEntity(name, IBundleEntity.KIND_TEMPLATE, referer);
		return getEvaluationStrategy().evaluate(t, context, arguments, referer);
	}

	public final void fireError(ILocatedEntity referer, String error) {
		collector.fireError(referer, error);
	}

	public void createStream(String name, String contents) {
		// ignore
	}
}
