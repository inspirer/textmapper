/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.objects.*;
import org.textway.templates.storage.Resource;

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
			report(KIND_ERROR, message, referer);
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
				report(KIND_ERROR, message, expr);
				throw ex;
			}
			return result;
		} catch (HandledEvaluationException ex) {
			throw ex;
		} catch (Exception th) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String message = "Evaluation of `" + expr.toString() + "` failed for " + getTitle(context.getThisObject()) + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			report(KIND_ERROR, message, expr);
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

	public IBundleEntity loadEntity(String qualifiedName, int kind, SourceElement referer) {
		return registry.loadEntity(qualifiedName, kind, referer);
	}

	public String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, SourceElement referer) {
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
		} catch (EvaluationException ex) {
			report(KIND_ERROR, ex.getMessage(), referer != null ? referer : t);
			return "";
		}
	}

	public Object evaluate(IQuery t, EvaluationContext context, Object[] arguments) throws EvaluationException {
		return t.invoke(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
	}

	public String eval(final Resource resource, EvaluationContext context) {
		TextSource source = new TextSource(resource.getUri().toString(), resource.getContents().toCharArray(), resource.getInitialLine());
		final TemplatesTree<TemplateNode> tree = TemplatesTree.parseBody(source, "syntax");
		for (final TemplatesProblem problem : tree.getErrors()) {
			DefaultEvaluationStrategy.this.report(KIND_ERROR, problem.getMessage(), new SourceElement() {
				public String getResourceName() {
					return resource.getUri().toString();
				}

				public int getOffset() {
					return resource.getInitialOffset() + problem.getOffset();
				}

				public int getEndOffset() {
					return resource.getInitialOffset() + problem.getEndOffset();
				}

				public int getLine() {
					return tree.getSource().lineForOffset(problem.getOffset());
				}
			});
		}

		ITemplate t = tree.getRoot();
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context), this, null);
		} catch (EvaluationException ex) {
			report(KIND_ERROR, ex.getMessage(), t);
			return "";
		}
	}


	public void report(int kind, String message, SourceElement... anchors) {
		templatesFacade.report(kind, message, anchors);
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
