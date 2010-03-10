package net.sf.lapg.templates.api.impl;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.ITemplate;

public class TemplatesFacade implements IProblemCollector {

	private IEvaluationStrategy evaluationStrategy;

	private final INavigationStrategy.Factory factory;
	private final IBundleLoader[] loaders;

	public TemplatesFacade(INavigationStrategy.Factory factory, IBundleLoader... loaders) {
		this.factory = factory;
		this.loaders = loaders;
	}

	private IEvaluationStrategy getEvaluationStrategy() {
		if(evaluationStrategy == null) {
			evaluationStrategy = createEvaluationStrategy(factory, createRegistry(loaders));
		}
		return evaluationStrategy;
	}

	protected IEvaluationStrategy createEvaluationStrategy(INavigationStrategy.Factory factory, TemplatesRegistry registry) {
		return new DefaultEvaluationStrategy(this, factory, registry);
	}

	protected TemplatesRegistry createRegistry(IBundleLoader... loaders) {
		return new TemplatesRegistry(this, loaders);
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		ITemplate t = (ITemplate) getEvaluationStrategy().loadEntity(name, IBundleEntity.KIND_TEMPLATE, referer);
		return getEvaluationStrategy().evaluate(t, context, arguments, referer);
	}

	public void fireError(ILocatedEntity referer, String error) {
		// ignore
	}

	public void createFile(String name, String contents) {
		// ignore
	}
}
