/*************************************************************
 * Copyright (c) 2002-2008 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg;

public interface IError {

	void error(String error);
	void warn(String warning);
	void debug(String info);
	void dispose();
}
