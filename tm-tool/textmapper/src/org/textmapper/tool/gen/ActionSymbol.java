/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
import org.textmapper.lapg.api.SymbolRef;
import org.textmapper.tool.compiler.LapgGrammar;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.EvaluationException;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.api.ITemplate;
import org.textmapper.templates.bundle.IBundleEntity;
import org.textmapper.templates.objects.DefaultIxObject;

public class ActionSymbol extends DefaultIxObject {

	final Symbol symbol;
	final SymbolRef ref;
	final boolean isLeft;
	final int rightOffset;
	private final IEvaluationStrategy evaluationStrategy;
	private final EvaluationContext context;
	private final String templatePackage;
	private final LapgGrammar grammar;

	public ActionSymbol(LapgGrammar grammar, Symbol symbol, SymbolRef ref, boolean isLeft, int rightOffset,
						IEvaluationStrategy strategy, EvaluationContext context, String templatePackage) {
		this.grammar = grammar;
		this.symbol = symbol;
		this.ref = ref;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		evaluationStrategy = strategy;
		this.context = context;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".symAccess",
				IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this, context), new Object[]{"sym"}, null);
	}

	@Override
	public Object getByIndex(Object index) throws EvaluationException {
		if (index instanceof String && ref != null) {
			return grammar.getAnnotation(ref, (String) index);
		}
		return super.getByIndex(index);
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
	public Object getProperty(String id) throws EvaluationException {
		if (id.equals("symbol")) {
			return symbol;
		}
		if (id.equals("isLeft")) {
			return isLeft;
		}
		if (id.equals("rightOffset")) {
			return rightOffset;
		}
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".symAccess",
				IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this), new Object[]{id}, null);
	}
}
