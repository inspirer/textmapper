/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.templates.eval;

import org.textmapper.templates.api.*;
import org.textmapper.templates.bundle.IBundleEntity;
import org.textmapper.templates.bundle.TemplatesRegistry;
import org.textmapper.templates.objects.IxFactory;

public class TemplatesFacade {

	private IEvaluationStrategy evaluationStrategy;

	private final IxFactory factory;
	private final TemplatesRegistry registry;
	private final TemplatesStatus status;

	public TemplatesFacade(IxFactory factory, TemplatesRegistry registry) {
		this(factory, registry, registry.getStatus());
	}

	public TemplatesFacade(IxFactory factory, TemplatesRegistry registry, TemplatesStatus status) {
		this.factory = factory;
		this.registry = registry;
		this.status = status;
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

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, SourceElement referer) {
		ITemplate t = (ITemplate) getEvaluationStrategy().loadEntity(name, IBundleEntity.KIND_TEMPLATE, referer);
		return getEvaluationStrategy().evaluate(t, context, arguments, referer);
	}

	public void report(int kind, String message, SourceElement... anchors) {
		status.report(kind, message, anchors);
	}

	public void createStream(String name, String contents) {
		// ignore
	}
}
