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
package org.textmapper.lapg.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.textmapper.lapg.common.ui.editor.StructuredTextEditor;
import org.textmapper.lapg.common.ui.editor.StructuredTextViewerConfiguration;
import org.textmapper.lapg.common.ui.editor.colorer.CommentScanner;
import org.textmapper.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import org.textmapper.lapg.common.ui.editor.colorer.ICommonColors;
import org.textmapper.lapg.ui.editor.colorer.ILapgColors;
import org.textmapper.lapg.ui.editor.colorer.LapgSourceScanner;

public final class LapgSourceViewerConfiguration extends StructuredTextViewerConfiguration {

	private final LapgSourceEditor fEditor;

	LapgSourceViewerConfiguration(LapgSourceEditor editor, IPreferenceStore preferenceStore) {
		super(editor, preferenceStore);
		fEditor = editor;
	}

	@Override
	protected IReconcilingStrategy createReconcilingStrategy() {
		return new LapgReconcilingStrategy(fEditor);
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new StructuredTextPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		final DefaultHighlightingManager cm = fEditor.getHighlightingManager();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new LapgSourceScanner(cm));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(new RuleBasedScanner() {
			{
				setDefaultReturnToken(cm.getColor(ICommonColors.COLOR_STRING).createToken());
			}
		});
		reconciler.setDamager(dr, IPartitions.LAPG_STRING);
		reconciler.setRepairer(dr, IPartitions.LAPG_STRING);

		dr = new DefaultDamagerRepairer(new RuleBasedScanner() {
			{
				setDefaultReturnToken(cm.getColor(ILapgColors.COLOR_REGEXP).createToken());
			}
		});
		reconciler.setDamager(dr, IPartitions.LAPG_REGEXP);
		reconciler.setRepairer(dr, IPartitions.LAPG_REGEXP);

		dr = new DefaultDamagerRepairer(new RuleBasedScanner() {
			{
				setDefaultReturnToken(cm.getColor(ILapgColors.COLOR_ACTIONS).createToken());
			}
		});
		reconciler.setDamager(dr, IPartitions.LAPG_ACTION);
		reconciler.setRepairer(dr, IPartitions.LAPG_ACTION);

		dr = new DefaultDamagerRepairer(new RuleBasedScanner() {
			{
				setDefaultReturnToken(cm.getColor(ICommonColors.COLOR_COMMENT_MULTI).createToken());
			}
		});
		reconciler.setDamager(dr, IPartitions.LAPG_TEMPLATES);
		reconciler.setRepairer(dr, IPartitions.LAPG_TEMPLATES);

		dr = new DefaultDamagerRepairer(new CommentScanner(cm, ICommonColors.COLOR_COMMENT_LINE));
		reconciler.setDamager(dr, IPartitions.LAPG_COMMENT_LINE);
		reconciler.setRepairer(dr, IPartitions.LAPG_COMMENT_LINE);

		return reconciler;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, IPartitions.LAPG_COMMENT_MULTI,
				IPartitions.LAPG_COMMENT_LINE, IPartitions.LAPG_STRING, IPartitions.LAPG_REGEXP,
				IPartitions.LAPG_TEMPLATES, IPartitions.LAPG_ACTION };
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IPartitions.LAPG_PARTITIONING;
	}

	@Override
	public IAnnotationHover getAnnotationHover(final ISourceViewer sourceViewer) {
		return new LapgProblemHover(sourceViewer);
	}

	@Override
	public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
		return new LapgProblemHover(sourceViewer);
	}

	@Override
	protected IHyperlinkDetector[] getSourceHyperlinkDetectors(StructuredTextEditor context) {
		return new IHyperlinkDetector[] { new LapgSourceHyperlinkDetector(context) };
	}
}
