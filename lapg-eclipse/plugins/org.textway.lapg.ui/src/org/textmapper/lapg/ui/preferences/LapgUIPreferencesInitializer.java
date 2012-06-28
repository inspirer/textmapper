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
package org.textway.lapg.ui.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.textway.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import org.textway.lapg.ui.LapgUIActivator;
import org.textway.lapg.ui.editor.colorer.LapgHighlightingManager;

public class LapgUIPreferencesInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = LapgUIActivator.getDefault().getPreferenceStore();

		EditorsUI.useAnnotationsPreferencePage(store);

		DefaultHighlightingManager manager = new LapgHighlightingManager(store, null);
		manager.initializeDefaults();
		manager.dispose();
	}
}
