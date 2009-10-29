package net.sf.lapg.templates.api.impl;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.ITemplateLoader;

public class TemplatesFacade implements IProblemCollector {

	private final IEvaluationStrategy evaluationStrategy;

	public TemplatesFacade(INavigationStrategy.Factory factory, ITemplateLoader... loaders) {
		evaluationStrategy = createEvaluationStrategy(factory, createRegistry(loaders));
	}

	protected IEvaluationStrategy createEvaluationStrategy(INavigationStrategy.Factory factory, TemplatesRegistry registry) {
		return new DefaultEvaluationStrategy(this, factory, registry);
	}

	protected TemplatesRegistry createRegistry(ITemplateLoader... loaders) {
		return new TemplatesRegistry(this, loaders);
	}

	public String executeTemplate(String name, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		return evaluationStrategy.executeTemplate(name, context, arguments, referer);
	}

	public void fireError(ILocatedEntity referer, String error) {
		// ignore
	}
}
