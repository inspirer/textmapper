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
package org.textway.lapg.common.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public abstract class StructuredTextViewerConfiguration extends TextSourceViewerConfiguration {

	public static final class StructuredTextPresentationReconciler extends PresentationReconciler {
		/**
		 * Last used document
		 */
		private IDocument fLastDocument;

		/**
		 * Constructs a "repair description" for the given damage and returns
		 * this description as a text presentation.
		 * <p>
		 * NOTE: Should not be used if this reconciler is installed on a viewer.
		 * </p>
		 */
		public TextPresentation createRepairDescription(IRegion damage, IDocument document) {
			if (document != fLastDocument) {
				setDocumentToDamagers(document);
				setDocumentToRepairers(document);
				fLastDocument = document;
			}
			return createPresentation(damage, document);
		}
	}

	private final StructuredTextEditor fEditor;

	public StructuredTextViewerConfiguration(StructuredTextEditor editor, IPreferenceStore preferenceStore) {
		super(preferenceStore);
		fEditor = editor;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		IReconcilingStrategy strategy = createReconcilingStrategy();

		StructuredTextReconciler reconciler = new StructuredTextReconciler(fEditor, strategy, false);
		reconciler.setDelay(500);
		return reconciler;
	}

	protected abstract IReconcilingStrategy createReconcilingStrategy();

	protected abstract IHyperlinkDetector[] getSourceHyperlinkDetectors(StructuredTextEditor context);

	@Override
	public final IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		IHyperlinkDetector[] registered = super.getHyperlinkDetectors(sourceViewer);
		return merge(registered, getSourceHyperlinkDetectors(fEditor));
	}

	private IHyperlinkDetector[] merge(IHyperlinkDetector[] array1, IHyperlinkDetector[] array2) {
		if (array1 != null && array2 != null) {
			IHyperlinkDetector[] allHyperlinkDetectors;
			int size = array1.length + array2.length;
			allHyperlinkDetectors = new IHyperlinkDetector[size];
			System.arraycopy(array1, 0, allHyperlinkDetectors, 0, array1.length);
			System.arraycopy(array2, 0, allHyperlinkDetectors, array1.length, array2.length);
			return allHyperlinkDetectors;
		}
		return array2 != null ? array2 : array1;
	}
}
