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
package net.sf.lapg;

/**
 * Reporting.
 */
public interface INotifier {

	void error(String error);

	void trace(Throwable ex);

	void warn(String warning);

	void debug(String info);

	void info(String info);

	void dispose();
}
