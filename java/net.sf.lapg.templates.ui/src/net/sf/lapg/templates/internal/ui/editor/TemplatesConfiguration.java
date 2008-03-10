package net.sf.lapg.templates.internal.ui.editor;

import net.sf.lapg.templates.internal.ui.editor.scan.SentenceScanner;
import net.sf.lapg.templates.internal.ui.editor.scan.TemplatesPartitionScanner;
import net.sf.lapg.templates.internal.ui.editor.scan.TemplatesTextScanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class TemplatesConfiguration extends SourceViewerConfiguration {

	private TemplatesDoubleClickStrategy doubleClickStrategy;

	private SentenceScanner sentenceScanner;

	private TemplatesTextScanner textScanner;

	private ColorManager colorManager;

	public TemplatesConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, TemplatesPartitionScanner.SENTENCE };
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (doubleClickStrategy == null) {
			doubleClickStrategy = new TemplatesDoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	protected TemplatesTextScanner getTextScanner() {
		if (textScanner == null) {
			textScanner = new TemplatesTextScanner(colorManager);
			textScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getTextColor())));
		}
		return textScanner;
	}

	protected SentenceScanner getSentenceScanner() {
		if (sentenceScanner == null) {
			sentenceScanner = new SentenceScanner(colorManager);
			sentenceScanner.setDefaultReturnToken(new Token(new TextAttribute(colorManager.getTemplateColor())));
		}
		return sentenceScanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getSentenceScanner());
		reconciler.setDamager(dr, TemplatesPartitionScanner.SENTENCE);
		reconciler.setRepairer(dr, TemplatesPartitionScanner.SENTENCE);

		dr = new DefaultDamagerRepairer(getTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

}