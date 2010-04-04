package net.sf.lapg.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import net.sf.lapg.ui.LapgUIActivator;
import net.sf.lapg.parser.LapgTree;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.parser.ast.AstRoot;

public class LapgReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private IProgressMonitor fMonitor;
	private IDocument fDocument;
	private final LapgSourceEditor fEditor;

	public LapgReconcilingStrategy(final LapgSourceEditor editor) {
		fEditor = editor;
	}

	public void setDocument(final IDocument document) {
		fDocument = document;
	}

	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcileInternal(false);
	}

	public void reconcile(final IRegion partition) {
		reconcileInternal(false);
	}

	public void setProgressMonitor(final IProgressMonitor monitor) {
		fMonitor = monitor;
	}

	public void initialReconcile() {
		reconcileInternal(true);
	}

	private void reconcileInternal(boolean first) {
		try {
			validate(fEditor, fDocument, fMonitor);
		} catch (Throwable ex) {
			ex.printStackTrace();
			LapgUIActivator.log(ex);
		}
	}

	private void validate(final LapgSourceEditor editor, final IDocument doc, IProgressMonitor monitor) {
		if (!checkEditor(editor)) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IFile mainResource = fEditor.getResource();
		String content = doc.get();

		TextSource input = new TextSource(mainResource.getName(), content.toCharArray(), 1);
		LapgTree<AstRoot> ast = LapgTree.parse(input);
		System.out.println("reconciled, " + ast.getErrors().size() + " errors");
	}

	private boolean checkEditor(final LapgSourceEditor editor) {
		return editor != null && editor.getDocumentProvider() != null;
	}
}
