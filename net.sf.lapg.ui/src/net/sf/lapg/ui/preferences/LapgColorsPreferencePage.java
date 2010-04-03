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
package net.sf.lapg.ui.preferences;

import net.sf.lapg.common.ui.editor.colorer.DefaultColorManager;
import net.sf.lapg.common.ui.preferences.DefaultColoringConfigurationBlock;
import net.sf.lapg.common.ui.preferences.OverlayPreferenceStore;
import net.sf.lapg.ui.LapgUIActivator;
import net.sf.lapg.ui.editor.colorer.LapgHighlightingManager;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

public class LapgColorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final DefaultColoringConfigurationBlock fConfigurationBlock;
	private OverlayPreferenceStore fOverlayStore;

	public LapgColorsPreferencePage() {
		setDescription();
		setPreferenceStore();
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), new OverlayPreferenceStore.OverlayKey[] {});
		fConfigurationBlock = createConfigurationBlock(fOverlayStore);
	}

	protected void setDescription() {
		setDescription("Synta&x");
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return null;
	}

	protected void setPreferenceStore() {
		setPreferenceStore(LapgUIActivator.getDefault().getPreferenceStore());
	}

	protected DefaultColoringConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new DefaultColoringConfigurationBlock(overlayPreferenceStore) {
			@Override
			protected LapgHighlightingManager createHighlightingManager(OverlayPreferenceStore store,
					DefaultColorManager colorManager) {
				return new LapgHighlightingManager(store, colorManager);
			}
		};
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		Control content = fConfigurationBlock.createControl(parent);

		initialize();

		Dialog.applyDialogFont(content);
		return content;
	}

	private void initialize() {
		fConfigurationBlock.initialize();
	}

	@Override
	public boolean performOk() {
		fOverlayStore.propagate();
		try {
			new InstanceScope().getNode(LapgUIActivator.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
		}
		return true;
	}

	@Override
	public void performDefaults() {
		fOverlayStore.loadDefaults();
		fConfigurationBlock.performDefaults();

		super.performDefaults();
	}

	@Override
	public void dispose() {
		fConfigurationBlock.dispose();

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}

		super.dispose();
	}
}
