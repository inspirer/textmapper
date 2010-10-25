package net.sf.lapg.common.ui.editor;

import net.sf.lapg.common.ui.LapgCommonActivator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

public abstract class StructuredTextReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private IProgressMonitor fMonitor;
	private IDocument fDocument;
	private final StructuredTextEditor fEditor;

	public StructuredTextReconcilingStrategy(final StructuredTextEditor editor) {
		fEditor = editor;
	}

	@Override
	public void setDocument(final IDocument document) {
		fDocument = document;
	}

	@Override
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcileInternal(false);
	}

	@Override
	public void reconcile(final IRegion partition) {
		reconcileInternal(false);
	}

	@Override
	public void setProgressMonitor(final IProgressMonitor monitor) {
		fMonitor = monitor;
	}

	@Override
	public void initialReconcile() {
		reconcileInternal(true);
	}

	private void reconcileInternal(boolean first) {
		ISourceStructure result = null;
		try {
			fEditor.aboutToBeReconciled();
			result = validate(first, fEditor, fDocument, fMonitor);

		} catch (Throwable ex) {
			ex.printStackTrace();
			LapgCommonActivator.log(ex);
		} finally {
			fEditor.reconciled(result, fMonitor);
		}
	}

	protected abstract ISourceStructure validate(boolean first, StructuredTextEditor editor, IDocument doc, IProgressMonitor monitor);
}
