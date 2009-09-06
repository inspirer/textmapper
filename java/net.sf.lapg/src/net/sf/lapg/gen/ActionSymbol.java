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
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationEnvironment;

public class ActionSymbol {

	final Symbol symbol;
	final boolean isLeft;
	final int rightOffset;
	private final IEvaluationEnvironment environment;
	private final String templatePackage;

	public ActionSymbol(Symbol symbol, boolean isLeft, int rightOffset, IEvaluationEnvironment environment, String templatePackage) {
		this.symbol = symbol;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.environment = environment;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		return environment.executeTemplate(templatePackage+".symbol", new EvaluationContext(this), null);
	}
}
