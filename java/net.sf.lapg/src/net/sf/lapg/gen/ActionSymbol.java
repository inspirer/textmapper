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
import net.sf.lapg.templates.api.ITemplatesFacade;

public class ActionSymbol {

	final Symbol symbol;
	final boolean isLeft;
	final int rightOffset;
	private final ITemplatesFacade facade;
	private final String templatePackage;

	public ActionSymbol(Symbol symbol, boolean isLeft, int rightOffset, ITemplatesFacade facade, String templatePackage) {
		this.symbol = symbol;
		this.isLeft = isLeft;
		this.rightOffset = rightOffset;
		this.facade = facade;
		this.templatePackage = templatePackage;
	}

	@Override
	public String toString() {
		return facade.executeTemplate(templatePackage+".symbol", new EvaluationContext(this), null, null);
	}
}
