package net.sf.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.StructuredTextEditor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Semantic highlighting reconciler - Background thread implementation.
 */
public class SemanticHighlightingReconciler implements ITextInputListener {

	/**
	 * Collects positions from the AST.
	 */
	public class PositionCollector {

		/** The semantic token */
		private final SemanticToken fToken = new SemanticToken();

		protected boolean visitNode(ASTNode node) {
			if ((node.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
				retainPositions(node.getStartPosition(), node.getLength());
				return false;
			}
			return true;
		}

		/**
		 * Add a position with the given range and highlighting iff it does not
		 * exist already.
		 */
		public void addPosition(int offset, int length, Highlighting highlighting) {
			boolean isExisting = false;
			// TODO: use binary search
			for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position = (HighlightedPosition) fRemovedPositions.get(i);
				if (position == null) {
					continue;
				}
				if (position.isEqual(offset, length, highlighting)) {
					isExisting = true;
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
					break;
				}
			}

			if (!isExisting) {
				Position position = fJobPresenter.createHighlightedPosition(offset, length, highlighting);
				fAddedPositions.add(position);
			}
		}

		/**
		 * Retain the positions completely contained in the given range.
		 */
		public void retainPositions(int offset, int length) {
			// TODO: use binary search
			for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position = fRemovedPositions.get(i);
				if (position != null && position.isContained(offset, length)) {
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
				}
			}
		}
	}

	/** Position collector */
	private final PositionCollector fCollector = new PositionCollector();

	/**
	 * The StructuredText editor this semantic highlighting reconciler is
	 * installed on
	 */
	private StructuredTextEditor fEditor;
	/** The source viewer this semantic highlighting reconciler is installed on */
	private ISourceViewer fSourceViewer;
	/** The semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlightings */
	private ColorDescriptor[] fSemanticHighlightings;

	/** Background job's added highlighted positions */
	private final List<HighlightedPosition> fAddedPositions = new ArrayList<HighlightedPosition>();
	/** Background job's removed highlighted positions */
	private List<HighlightedPosition> fRemovedPositions = new ArrayList<HighlightedPosition>();
	/** Number of removed positions */
	private int fNOfRemovedPositions;

	/** Background job */
	private Job fJob;
	/** Background job lock */
	private final Object fJobLock = new Object();
	/**
	 * Reconcile operation lock.
	 */
	private final Object fReconcileLock = new Object();
	/**
	 * <code>true</code> if any thread is executing <code>reconcile</code>,
	 */
	private boolean fIsReconciling = false;

	/**
	 * The semantic highlighting presenter - cache for background thread, only
	 * valid during
	 * {@link #reconciled(CompilationUnit, boolean, IProgressMonitor)}
	 */
	private SemanticHighlightingPresenter fJobPresenter;
	/**
	 * Semantic highlightings - cache for background thread, only valid during
	 * {@link #reconciled(CompilationUnit, boolean, IProgressMonitor)}
	 */
	private ColorDescriptor[] fJobSemanticHighlightings;
	/**
	 * Highlightings - cache for background thread, only valid during
	 * {@link #reconciled(CompilationUnit, boolean, IProgressMonitor)}
	 */
	private Highlighting[] fJobHighlightings;

	/**
	 * XXX Hack for performance reasons (should loop over
	 * fJobSemanticHighlightings can call consumes(*))
	 *
	 * @since 3.5
	 */
	private Highlighting fJobDeprecatedMemberHighlighting;

	public void aboutToBeReconciled() {
		// Do nothing
	}

	public void reconciled(ISourceStructure model, boolean forced, IProgressMonitor progressMonitor) {
		// ensure at most one thread can be reconciling at any time
		synchronized (fReconcileLock) {
			if (fIsReconciling) {
				return;
			} else {
				fIsReconciling = true;
			}
		}
		fJobPresenter = fPresenter;
		fJobSemanticHighlightings = fSemanticHighlightings;
		fJobHighlightings = fHighlightings;

		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null || fJobHighlightings == null) {
				return;
			}

			fJobPresenter.setCanceled(progressMonitor.isCanceled());

			if (ast == null || fJobPresenter.isCanceled()) {
				return;
			}

			startReconcilingPositions();

			if (!fJobPresenter.isCanceled()) {
				fJobDeprecatedMemberHighlighting = null;
				for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
					SemanticHighlighting semanticHighlighting = fJobSemanticHighlightings[i];
					if (fJobHighlightings[i].isEnabled()
							&& semanticHighlighting instanceof DeprecatedMemberHighlighting) {
						fJobDeprecatedMemberHighlighting = fJobHighlightings[i];
						break;
					}
				}
				reconcilePositions(subtrees);
			}

			TextPresentation textPresentation = null;
			if (!fJobPresenter.isCanceled()) {
				textPresentation = fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
			}

			if (!fJobPresenter.isCanceled()) {
				updatePresentation(textPresentation, fAddedPositions, fRemovedPositions);
			}

			stopReconcilingPositions();
		} finally {
			fJobPresenter = null;
			fJobSemanticHighlightings = null;
			fJobHighlightings = null;
			fJobDeprecatedMemberHighlighting = null;
			synchronized (fReconcileLock) {
				fIsReconciling = false;
			}
		}
	}

	/**
	 * Start reconciling positions.
	 */
	private void startReconcilingPositions() {
		fJobPresenter.addAllPositions(fRemovedPositions);
		fNOfRemovedPositions = fRemovedPositions.size();
	}

	/**
	 * Reconcile positions based on the AST subtrees
	 */
	private void reconcilePositions(ASTNode[] subtrees) {
		// FIXME: remove positions not covered by subtrees

		for (int i = 0, n = subtrees.length; i < n; i++) {
			subtrees[i].accept(fCollector);
		}
		List<HighlightedPosition> oldPositions = fRemovedPositions;
		List<HighlightedPosition> newPositions = new ArrayList<HighlightedPosition>(fNOfRemovedPositions);
		for (int i = 0, n = oldPositions.size(); i < n; i++) {
			Object current = oldPositions.get(i);
			if (current != null) {
				newPositions.add(current);
			}
		}
		fRemovedPositions = newPositions;
	}

	/**
	 * Update the presentation.
	 */
	private void updatePresentation(TextPresentation textPresentation, List<HighlightedPosition> addedPositions,
			List<HighlightedPosition> removedPositions) {
		Runnable runnable = fJobPresenter.createUpdateRunnable(textPresentation, addedPositions, removedPositions);
		if (runnable == null) {
			return;
		}

		StructuredTextEditor editor = fEditor;
		if (editor == null) {
			return;
		}

		IWorkbenchPartSite site = editor.getSite();
		if (site == null) {
			return;
		}

		Shell shell = site.getShell();
		if (shell == null || shell.isDisposed()) {
			return;
		}

		Display display = shell.getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}

		display.asyncExec(runnable);
	}

	/**
	 * Stop reconciling positions.
	 */
	private void stopReconcilingPositions() {
		fRemovedPositions.clear();
		fNOfRemovedPositions = 0;
		fAddedPositions.clear();
	}

	/**
	 * Install this reconciler on the given editor, presenter and highlightings.
	 */
	public void install(StructuredTextEditor editor, ISourceViewer sourceViewer,
			SemanticHighlightingPresenter presenter, SemanticHighlighting[] semanticHighlightings,
			Highlighting[] highlightings) {
		fPresenter = presenter;
		fSemanticHighlightings = semanticHighlightings;
		fHighlightings = highlightings;

		fEditor = editor;
		fSourceViewer = sourceViewer;

		if (fEditor instanceof CompilationUnitEditor) {
			((CompilationUnitEditor) fEditor).addReconcileListener(this);
		} else if (fEditor == null) {
			fSourceViewer.addTextInputListener(this);
			scheduleJob();
		}
	}

	/**
	 * Uninstall this reconciler from the editor
	 */
	public void uninstall() {
		if (fPresenter != null) {
			fPresenter.setCanceled(true);
		}

		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			fEditor = null;
		}

		fSourceViewer = null;
		fSemanticHighlightings = null;
		fHighlightings = null;
		fPresenter = null;
	}

	/**
	 * Schedule a background job for retrieving the AST and reconciling the
	 * Semantic Highlighting model.
	 */
	private void scheduleJob() {
		synchronized (fJobLock) {
			final Job oldJob = fJob;
			if (fJob != null) {
				fJob.cancel();
				fJob = null;
			}

			fJob = new Job("Semantic Highlighting Job") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (oldJob != null) {
						try {
							oldJob.join();
						} catch (InterruptedException e) {
							StructuredTextPlugin.log(e);
							return Status.CANCEL_STATUS;
						}
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					CompilationUnit ast = SharedASTProvider.getAST(element, SharedASTProvider.WAIT_YES, monitor);
					reconciled(ast, false, monitor);
					synchronized (fJobLock) {
						// allow the job to be gc'ed
						if (fJob == this) {
							fJob = null;
						}
					}
					return Status.OK_STATUS;
				}
			};
			fJob.setSystem(true);
			fJob.setPriority(Job.DECORATE);
			fJob.schedule();
		}
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		synchronized (fJobLock) {
			if (fJob != null) {
				fJob.cancel();
				fJob = null;
			}
		}
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null) {
			scheduleJob();
		}
	}

	/**
	 * Refreshes the highlighting.
	 */
	public void refresh() {
		scheduleJob();
	}
}
