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
package net.sf.lapg.ui.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.StructuredTextEditor;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.ui.LapgUIActivator;
import net.sf.lapg.ui.editor.colorer.LapgHighlightingManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class LapgSourceEditor extends StructuredTextEditor {

	private DefaultHighlightingManager fHighlightingManager;

	public LapgSourceEditor() {
		setRulerContextMenuId("#LapgSourceEditorContext"); //$NON-NLS-1$
		setEditorContextMenuId("#LapgSourceEditorContext"); //$NON-NLS-1$
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer = getSourceViewer();
		if (!(sourceViewer instanceof ISourceViewerExtension2)) {
			setPreferenceStore(createCombinedPreferenceStore(input));
			internalDoSetInput(input);
			return;
		}

		// uninstall & unregister preference store listener
		getSourceViewerDecorationSupport(sourceViewer).uninstall();
		((ISourceViewerExtension2) sourceViewer).unconfigure();

		setPreferenceStore(createCombinedPreferenceStore(input));

		// install & register preference store listener
		sourceViewer.configure(getSourceViewerConfiguration());
		getSourceViewerDecorationSupport(sourceViewer).install(getPreferenceStore());

		internalDoSetInput(input);
	}

	private void internalDoSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		LapgSourceViewer sourceViewer = null;
		if (getSourceViewer() instanceof LapgSourceViewer) {
			sourceViewer = (LapgSourceViewer) getSourceViewer();
		}

		if (sourceViewer != null && sourceViewer.getReconciler() == null) {
			IReconciler reconciler = getSourceViewerConfiguration().getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				sourceViewer.setReconciler(reconciler);
			}
		}
	}

	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(3);
		stores.add(LapgUIActivator.getDefault().getPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "net.sf.lapg.ui.scope" }); //$NON-NLS-1$
	}

	@Override
	protected void initializeEditor() {
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		setSourceViewerConfiguration(new LapgSourceViewerConfiguration(this, getPreferenceStore()));
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new LapgSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	@Override
	public void dispose() {
		if (fHighlightingManager != null) {
			fHighlightingManager.dispose();
			fHighlightingManager = null;
		}
		super.dispose();
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		try {
			if (fHighlightingManager != null) {
				fHighlightingManager.propertyChange(event);
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	@Override
	protected String[] collectContextMenuPreferencePages() {
		String[] inheritedPages = super.collectContextMenuPreferencePages();
		String[] result = new String[inheritedPages.length + 2];
		result[0] = "net.sf.lapg.ui.preferences.RootPreferencePage"; //$NON-NLS-1$
		result[1] = "net.sf.lapg.ui.preferences.ColorPreferencePage"; //$NON-NLS-1$
		System.arraycopy(inheritedPages, 0, result, 2, inheritedPages.length);
		return result;
	}

	@Override
	public void createPartControl(Composite parent) {
		fHighlightingManager = new LapgHighlightingManager(getPreferenceStore(), LapgUIActivator.getDefault()
				.getColorManager());
		super.createPartControl(parent);
	}

	DefaultHighlightingManager getHighlightingManager() {
		return fHighlightingManager;
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		if (fHighlightingManager != null) {
			if (fHighlightingManager.isAffected(event)) {
				return true;
			}
		}
		return super.affectsTextPresentation(event);
	}

	public IFile getResource() {
		if (getEditorInput() instanceof FileEditorInput) {
			return ((FileEditorInput) getEditorInput()).getFile();
		}
		return null;
	}
}
