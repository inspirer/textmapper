/**
 * This file is part of Lapg.UI project.
 * 
 * Copyright (c) 2010 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 */
package net.sf.lapg.common.ui.editor.colorer;

import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.ColorDescriptor;

public interface IHighlightingStyleListener {

	/**
	 *  Text presentation options changed for the given descriptor.
	 */
	void highlightingStyleChanged(ColorDescriptor cd);
}