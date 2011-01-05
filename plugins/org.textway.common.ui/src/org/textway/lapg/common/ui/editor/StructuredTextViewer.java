package org.textway.lapg.common.ui.editor;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

public class StructuredTextViewer extends SourceViewer {

	public StructuredTextViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean showAnnotationsOverview, int styles) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
	}

	/**
	 * Prepends the text presentation listener at the beginning of the viewer's
	 * list of text presentation listeners.If the listener is already
	 *  registered with the viewer this call moves the listener to the beginning
	 *  of the list.
	 */
	@SuppressWarnings("unchecked")
	public void prependTextPresentationListener(ITextPresentationListener listener) {

		if (fTextPresentationListeners == null) {
			fTextPresentationListeners = new ArrayList<ITextPresentationListener>();
		}

		fTextPresentationListeners.remove(listener);
		fTextPresentationListeners.add(0, listener);
	}

	void setReconciler(IReconciler reconciler) {
		fReconciler = reconciler;
	}

	public IReconciler getReconciler() {
		return fReconciler;
	}
}
