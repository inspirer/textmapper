/*************************************************************
 * Copyright (c) 2002-2009 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
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
