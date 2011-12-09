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
package org.textway.lapg.gen;

import org.textway.lapg.api.Symbol;
import org.textway.lapg.api.SymbolRef;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.api.ITemplate;
import org.textway.templates.bundle.IBundleEntity;
import org.textway.templates.objects.DefaultIxObject;

public class ActionSymbol extends DefaultIxObject {

	final Symbol symbol;
	final SymbolRef ref;
	final boolean isLeft;
	final int rightOffset;
	private final IEvaluationStrategy evaluationStrategy;
	private final EvaluationContext context;
	private final String templatePackage;

	public ActionSymbol(Symbol symbol, SymbolRef ref, boolean isLeft, int rightOffset, IEvaluationStrategy strategy, EvaluationContext context, String templatePackage) {
		this.symbol = symbol;
		this.ref = ref;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.evaluationStrategy = strategy;
		this.context = context;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage + ".symAccess", IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this, context), new Object[] { "sym" }, null);
	}

	public Object getByIndex(Object index) throws EvaluationException {
		if (index instanceof String && ref != null) {
			return ref.getAnnotation((String) index);
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
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this), new Object[] { id }, null);
	}
}
