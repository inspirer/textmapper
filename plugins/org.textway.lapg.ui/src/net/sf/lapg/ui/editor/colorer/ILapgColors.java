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
package net.sf.lapg.ui.editor.colorer;

import org.textway.lapg.common.ui.editor.colorer.ICommonColors;

public interface ILapgColors extends ICommonColors {

	String LAPG_COLORS_KEY_PREFIX = "lapg_source_editor.color."; //$NON-NLS-1$

	String COLOR_REGEXP = "color_regexp"; //$NON-NLS-1$
	String COLOR_ACTIONS = "color_actions"; //$NON-NLS-1$

	String COLOR_REFERENCE = "color_reference"; //$NON-NLS-1$
	String COLOR_ELEMENTID = "color_elementid"; //$NON-NLS-1$
}
