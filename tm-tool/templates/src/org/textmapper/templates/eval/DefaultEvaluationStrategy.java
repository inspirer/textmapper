/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
import org.textmapper.templates.api.types.ITypesRegistry;
import org.textmapper.templates.ast.ExpressionNode;
import org.textmapper.templates.ast.TemplateNode;
import org.textmapper.templates.ast.TemplatesTree;
import org.textmapper.templates.ast.TemplatesTree.TemplatesProblem;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.bundle.IBundleEntity;
import org.textmapper.templates.bundle.TemplatesRegistry;
import org.textmapper.templates.objects.IxAdaptable;
import org.textmapper.templates.objects.IxFactory;
import org.textmapper.templates.objects.IxObject;
import org.textmapper.templates.objects.IxOperand;
import org.textmapper.templates.storage.Resource;

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

	@Override
	public String toString(Object o, ExpressionNode referer) throws EvaluationException {
		if (o instanceof Collection<?> || o instanceof Object[]) {
			String message = "Evaluation of `" + referer.toString() + "` results in collection, cannot convert to String";
			EvaluationException ex = new HandledEvaluationException(message);
			report(KIND_ERROR, message, referer);
			throw ex;
		}
		return o.toString();
	}

	@Override
	public Object evaluate(ExpressionNode expr, EvaluationContext context, boolean permitNull) throws EvaluationException {
		try {
			Object result = expr.evaluate(context, this);
			if (result == null && !permitNull) {
				String message = "Evaluation of `" + expr.toString() + "` failed for " + getTitle(context.getThisObject()) + ": null";
				EvaluationException ex = new HandledEvaluationException(message);
				report(KIND_ERROR, message, expr);
//				context.printStackTrace(expr, System.err);
				throw ex;
			}
			return result;
		} catch (HandledEvaluationException ex) {
			throw ex;
		} catch (Exception th) {
			Throwable cause = th.getCause() != null ? th.getCause() : th;
			String with = "";
			if (!(cause instanceof EvaluationException)) {
				with = " with " + cause.getClass().getName();
			}
			String message = "Evaluation of `" + expr.toString() + "` failed for " + getTitle(context.getThisObject()) + with + ": " + cause.getMessage();
			EvaluationException ex = new HandledEvaluationException(message);
			report(KIND_ERROR, message, expr);
//			context.printStackTrace(expr, System.err);
			throw ex;
		}
	}

	@Override
	public String getTitle(Object object) {
		if (object == null) {
			return "<unknown>";
		}
		if (object instanceof INamedEntity) {
			return ((INamedEntity) object).getTitle();
		}
		return object.getClass().getCanonicalName();
	}

	@Override
	public IxObject asObject(Object o) {
		return navigationFactory.asObject(o);
	}

	@Override
	public IxOperand asOperand(Object o) {
		return navigationFactory.asOperand(o);
	}

	@Override
	public IxAdaptable asAdaptable(Object o) {
		return navigationFactory.asAdaptable(o);
	}

	@Override
	public void setStrategy(IEvaluationStrategy strategy) {
	}

	private static class HandledEvaluationException extends EvaluationException {

		private static final long serialVersionUID = -718162932392225590L;

		public HandledEvaluationException(String message) {
			super(message);
		}
	}

	@Override
	public IBundleEntity loadEntity(String qualifiedName, int kind, SourceElement referer) {
		return registry.loadEntity(qualifiedName, kind, referer);
	}

	@Override
	public String evaluate(ITemplate t, EvaluationContext context, Object[] arguments, SourceElement caller) {
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, caller, context, t), this, arguments);
		} catch (EvaluationException ex) {
			report(KIND_ERROR, ex.getMessage(), caller != null ? caller : t);
			return "";
		}
	}

	@Override
	public Object evaluate(IQuery t, EvaluationContext context, Object[] arguments, SourceElement caller) throws EvaluationException {
		return t.invoke(new EvaluationContext(context != null ? context.getThisObject() : null, caller, context, t), this, arguments);
	}

	@Override
	public String eval(final Resource resource, EvaluationContext context) {
		TextSource source = new TextSource(resource.getUri().toString(), resource.getContents(), resource.getInitialLine());
		final TemplatesTree<TemplateNode> tree = TemplatesTree.parseBody(source, "syntax");
		for (final TemplatesProblem problem : tree.getErrors()) {
			DefaultEvaluationStrategy.this.report(KIND_ERROR, problem.getMessage(), new SourceElement() {
				@Override
				public String getResourceName() {
					return resource.getUri().toString();
				}

				@Override
				public int getOffset() {
					return resource.getInitialOffset() + problem.getOffset();
				}

				@Override
				public int getEndOffset() {
					return resource.getInitialOffset() + problem.getEndoffset();
				}

				@Override
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
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, null, context), this, null);
		} catch (EvaluationException ex) {
			report(KIND_ERROR, ex.getMessage(), t);
			return "";
		}
	}


	@Override
	public void report(int kind, String message, SourceElement... anchors) {
		templatesFacade.report(kind, message, anchors);
	}

	@Override
	public final void createStream(String name, String contents) {
		templatesFacade.createStream(name, contents);
	}

	@Override
	public ITypesRegistry getTypesRegistry() {
		return registry.getTypesRegistry();
	}

	@Override
	public IEvaluationCache getCache() {
		if (myCache == null) {
			myCache = new DefaultEvaluationCache();
		}
		return myCache;
	}
}
