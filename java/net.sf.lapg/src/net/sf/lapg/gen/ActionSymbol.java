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
package net.sf.lapg.gen;

import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ITemplate;

public class ActionSymbol {

	final Symbol symbol;
	final SymbolRef ref;
	final boolean isLeft;
	final int rightOffset;
	private final IEvaluationStrategy evaluationStrategy;
	private final String templatePackage;

	public ActionSymbol(Symbol symbol, SymbolRef ref, boolean isLeft, int rightOffset, IEvaluationStrategy strategy, String templatePackage) {
		this.symbol = symbol;
		this.ref = ref;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.evaluationStrategy = strategy;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		ITemplate templ = (ITemplate) evaluationStrategy.loadEntity(templatePackage+".symbol", IBundleEntity.KIND_TEMPLATE, null);
		return evaluationStrategy.evaluate(templ, new EvaluationContext(this), null, null);
	}
}
