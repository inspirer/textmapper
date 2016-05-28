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
package org.textmapper.tool.gen;

import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.templates.api.*;
import org.textmapper.tool.compiler.TMDataUtil;
import org.textmapper.tool.compiler.TMGrammar;
import org.textmapper.templates.bundle.IBundleEntity;
import org.textmapper.templates.objects.DefaultIxObject;

public class ActionSymbol extends DefaultIxObject {

	final Symbol symbol;
	final RhsSymbol ref;
	final boolean isLeft;
	final int rightOffset;
	final int leftOffset;
	private final IEvaluationStrategy evaluationStrategy;
	private final EvaluationContext context;
	private final String templatePackage;
	private final SourceElement caller;
	private final TMGrammar grammar;

	public ActionSymbol(TMGrammar grammar, Symbol symbol, RhsSymbol ref, boolean isLeft, int rightOffset,
						int leftOffset, IEvaluationStrategy strategy, EvaluationContext context, String templatePackage,
						SourceElement caller) {
		this.grammar = grammar;
		this.symbol = symbol;
		this.ref = ref;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.leftOffset = leftOffset;
		evaluationStrategy = strategy;
		this.context = context;
		this.templatePackage = templatePackage;
		this.caller = caller;
	}

	@Override
	public String toString() {
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".symAccess",
				IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this, caller, context), new Object[]{"value"}, null);
	}

	@Override
	public Object getByIndex(SourceElement caller, Object index) throws EvaluationException {
		if (index instanceof String && ref != null) {
			return grammar.getAnnotation(ref, (String) index);
		}
		return super.getByIndex(caller, index);
	}

	@Override
	protected String getType() {
		return "[rule symbol reference]";
	}

	@Override
	public boolean is(String qualifiedName) throws EvaluationException {
		return false;
	}

	@Override
	public Object getProperty(SourceElement caller, String id) throws EvaluationException {
		if (id.equals("symbol")) {
			return symbol;
		}
		if (id.equals("isLeft")) {
			return isLeft;
		}
		if (id.equals("rightOffset")) {
			return rightOffset;
		}
		if (id.equals("leftOffset")) {
			return leftOffset;
		}
		if (id.equals("role")) {
			return TMDataUtil.getRole(ref);
		}
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".symAccess",
				IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this, caller, context), new Object[]{id}, null);
	}
}
