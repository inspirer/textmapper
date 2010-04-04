package net.sf.lapg.ui.editor;

import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

public class LapgSourceViewer extends SourceViewer {

	public LapgSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean showAnnotationsOverview, int styles) {
		super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
	}

	void setReconciler(IReconciler reconciler) {
		fReconciler = reconciler;
	}

	IReconciler getReconciler() {
		return fReconciler;
	}
}
