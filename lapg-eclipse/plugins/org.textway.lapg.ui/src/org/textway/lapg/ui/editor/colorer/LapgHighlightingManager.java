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
package org.textway.lapg.ui.editor.colorer;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.textway.lapg.common.ui.editor.colorer.ColorDefinition;
import org.textway.lapg.common.ui.editor.colorer.DefaultColorManager;
import org.textway.lapg.common.ui.editor.colorer.DefaultHighlightingManager;

public class LapgHighlightingManager extends DefaultHighlightingManager {

	public LapgHighlightingManager(IPreferenceStore store, DefaultColorManager manager) {
		super(store, manager, ILapgColors.LAPG_COLORS_KEY_PREFIX);
	}

	@Override
	protected void initColors(List<ColorDefinition> colors) {
		super.initColors(colors);
		colors.add(new ColorDefinition(ILapgColors.COLOR_REGEXP, "Regular Expressions", "main", false, new RGB(0, 69, 110), false, false, false, false));
		colors.add(new ColorDefinition(ILapgColors.COLOR_ACTIONS, "Semantic Actions", "main", false, new RGB(16, 53, 62), false, false, false, false));
		colors.add(new ColorDefinition(ILapgColors.COLOR_REFERENCE, "References", "main", true, new RGB(0, 0, 0), false, false, false, false));
		colors.add(new ColorDefinition(ILapgColors.COLOR_ELEMENTID, "Element Identifiers", "main", true, new RGB(81, 3, 89), false, false, false, false));
	}

	@Override
	public ColorDescriptor[] getSemanticHighlightings() {
		return new ColorDescriptor[] { getColor(ILapgColors.COLOR_ELEMENTID),
				getColor(ILapgColors.COLOR_REFERENCE), };
	}
}
