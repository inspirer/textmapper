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

import org.textway.templates.api.*;
import org.textway.templates.api.types.ITypesRegistry;
import org.textway.templates.ast.TemplatesTree;
import org.textway.templates.ast.TemplatesTree.TemplatesProblem;
import org.textway.templates.ast.TemplatesTree.TextSource;
import org.textway.templates.ast.ExpressionNode;
import org.textway.templates.ast.TemplateNode;
import org.textway.templates.bundle.IBundleEntity;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.objects.*;

import java.util.Collection;

public class DefaultEvaluationStrategy implements IEvaluationStrategy {

	private final TemplatesFacade templatesFacade;
	private final TemplatesRegistry registry;
	private final IxFactory navigationFactory;
	private IEvaluationCache myCache;

	public DefaultEvaluationStrategy(TemplatesFacade facade, IxFactory factory, TemplatesRegistry registry) {
		this.templatesFacade = facade;
		this.navigationFactory = factory;
		this.registry = registry;
		factory.setStrategy(this);
	}

	public String toString(Object o, ExpressionNode referer) throws EvaluationException {
		if (o instanceof Collection<?> || o instanceof Object[]) {
			String message = "Evaluation of `" + referer.toString() + "` results in collection, cannot convert to String";
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(referer, message);
			throw ex;
		}
		return o.toString();
	}

	public Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if (result == null && !permitNull) {
				String message = "Evaluation of `" + expr.toString() + "` failed for " + getTitle(context.getThisObject()) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				fireError(expr, message);
				throw ex;
			}
			return result;
		} catch (HandledEvaluationException ex) {
			throw ex;
		} catch (Exception th) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `" + expr.toString() + "` failed for " + getTitle(context.getThisObject()) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			fireError(expr, message);
			throw ex;
		}
	}

	public String getTitle(Object object) {
		if (object == null) {
			return "<unknown>";
		}
		if (object instanceof INamedEntity) {
			return ((INamedEntity) object).getTitle();
		}
		return object.getClass().getCanonicalName();
	}

	public IxObject asObject(Object o) {
		return navigationFactory.asObject(o);
	}

	public IxOperand asOperand(Object o) {
		return navigationFactory.asOperand(o);
	}

	public IxAdaptable asAdaptable(Object o) {
		return navigationFactory.asAdaptable(o);
	}

	public void setStrategy(IEvaluationStrategy strategy) {
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}

	public IBundleEntity loadEntity(String qualifiedName, int kind, ILocatedEntity referer) {
		return registry.loadEntity(qualifiedName, kind, referer);
	}

	public String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) {
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, ILocatedEntity referer) throws EvaluationException {
		return t.invoke(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
	}

	public String eval(ILocatedEntity referer, String template, String templateId, EvaluationContext context, int line) {
		final String sourceName = templateId != null ? templateId : referer.getLocation();
		TemplatesTree<TemplateNode> tree = TemplatesTree.parseBody(new TextSource(sourceName, template.toCharArray(), line), "syntax");
		for (TemplatesProblem problem : tree.getErrors()) {
			final int errline = tree.getSource().lineForOffset(problem.getOffset());
			DefaultEvaluationStrategy.this.fireError(new ILocatedEntity() {
				public String getLocation() {
					return sourceName + "," + errline;
				}
			}, problem.getMessage());
		}

		ITemplate t = tree.getRoot();
		if (t == null) {
			return "";
		}
		try {
			return t.apply(context, this, null);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}


	public void fireError(ILocatedEntity referer, String error) {
		templatesFacade.fireError(referer, error);
	}

	public final void createStream(String name, String contents) {
		templatesFacade.createStream(name, contents);
	}

	public ITypesRegistry getTypesRegistry() {
		return registry.getTypesRegistry();
	}

	public IEvaluationCache getCache() {
		if (myCache == null) {
			myCache = new DefaultEvaluationCache();
		}
		return myCache;
	}
}
