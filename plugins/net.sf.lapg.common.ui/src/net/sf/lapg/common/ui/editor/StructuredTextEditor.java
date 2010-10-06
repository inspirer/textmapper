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
package net.sf.lapg.common.ui.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.StructuredTextReconciler.IReconcilingListener;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.common.ui.editor.colorer.ISemanticHighlighter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public abstract class StructuredTextEditor extends TextEditor {

	private DefaultHighlightingManager fHighlightingManager;

	public StructuredTextEditor() {
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

		StructuredTextViewer sourceViewer = null;
		if (getSourceViewer() instanceof StructuredTextViewer) {
			sourceViewer = (StructuredTextViewer) getSourceViewer();
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
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(2);
		stores.add(getPluginPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
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
		setSourceViewerConfiguration(createSourceViewerConfiguration());
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
	public void dispose() {
		if (fHighlightingManager != null) {
			fHighlightingManager.dispose();
			fHighlightingManager = null;
		}
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		fHighlightingManager = createHighlightingManager();
		super.createPartControl(parent);
	}

	public void aboutToBeReconciled() {
	}

	public void reconciled(ISourceStructure result, IProgressMonitor progressMonitor) {
	}

	public DefaultHighlightingManager getHighlightingManager() {
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

	public StructuredTextViewerConfiguration createSourceViewerConfiguration() {
		return new StructuredTextViewerConfiguration(getPreferenceStore());
	}

	public final void addReconcileListener(IReconcilingListener listener) {
	}

	public final void removeReconcileListener(IReconcilingListener listener) {
	}

	public abstract ISemanticHighlighter createSemanticHighlighter();

	public abstract ISourceStructure getModel();

	protected abstract DefaultHighlightingManager createHighlightingManager();

	/**
	 * return YourPluginActivator.getInstance().getPreferenceStore()
	 */
	protected abstract IPreferenceStore getPluginPreferenceStore();
}

