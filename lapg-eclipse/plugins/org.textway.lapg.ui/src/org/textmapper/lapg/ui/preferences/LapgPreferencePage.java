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


import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.textway.lapg.ui.LapgUIActivator;

public class LapgPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LapgPreferencePage() {
		setPreferenceStore(LapgUIActivator.getDefault().getPreferenceStore());
		setTitle("General settings for Lapg Source Editor");
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
	}
}
