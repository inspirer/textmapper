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

import net.sf.lapg.common.ui.editor.ISourceStructure;
import net.sf.lapg.common.ui.editor.StructuredTextEditor;
import net.sf.lapg.common.ui.editor.StructuredTextReconciler;
import net.sf.lapg.common.ui.editor.StructuredTextReconciler.IReconcilingListener;
import net.sf.lapg.common.ui.editor.StructuredTextViewerConfiguration;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import net.sf.lapg.ui.LapgUIActivator;
import net.sf.lapg.ui.editor.colorer.LapgHighlightingManager;
import net.sf.lapg.ui.editor.colorer.LapgSemanticHighlighter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.FileEditorInput;

public class LapgSourceEditor extends StructuredTextEditor implements IReconcilingListener {

	public LapgSourceEditor() {
		super();
		setRulerContextMenuId("#LapgSourceEditorContext"); //$NON-NLS-1$
		setEditorContextMenuId("#LapgSourceEditorContext"); //$NON-NLS-1$
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "net.sf.lapg.ui.scope" }); //$NON-NLS-1$
	}

	public IAnnotationModel getAnnotationModel() {
		ISourceViewer sourceViewer = getSourceViewer();
		return sourceViewer != null ? sourceViewer.getAnnotationModel() : null;
	}

	@Override
	public StructuredTextViewerConfiguration createSourceViewerConfiguration() {
		return new LapgSourceViewerConfiguration(this, getPreferenceStore());
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
	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
		super.performSave(overwrite, progressMonitor);
		forceReconciling();
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

	public IFile getResource() {
		if (getEditorInput() instanceof FileEditorInput) {
			return ((FileEditorInput) getEditorInput()).getFile();
		}
		return null;
	}

	public synchronized void forceReconciling() {
		ISourceViewer viewer = getSourceViewer();
		if (viewer instanceof LapgSourceViewer) {
			IReconciler reconciler = ((LapgSourceViewer) viewer).getReconciler();
			if (reconciler instanceof StructuredTextReconciler) {
				((StructuredTextReconciler) reconciler).performReconciling();
			}
		}
	}

	@Override
	public ISemanticHighlighter createSemanticHighlighter() {
		return new LapgSemanticHighlighter();
	}

	@Override
	protected DefaultHighlightingManager createHighlightingManager() {
		return new LapgHighlightingManager(getPreferenceStore(), LapgUIActivator.getDefault()
				.getColorManager());
	}

	@Override
	protected IPreferenceStore getPluginPreferenceStore() {
		return LapgUIActivator.getDefault().getPreferenceStore();
	}
}
