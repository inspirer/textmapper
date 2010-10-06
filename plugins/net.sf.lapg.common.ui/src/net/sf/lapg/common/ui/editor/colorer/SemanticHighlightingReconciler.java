package net.sf.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.LapgCommonActivator;
import net.sf.lapg.common.ui.editor.ISourceStructure;
import net.sf.lapg.common.ui.editor.StructuredTextEditor;
import net.sf.lapg.common.ui.editor.StructuredTextReconciler.IReconcilingListener;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.ColorDescriptor;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.Highlighting;
import net.sf.lapg.common.ui.editor.colorer.SemanticHighlightingManager.HighlightedPosition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Semantic highlighting reconciler - Background thread implementation.
 */
public class SemanticHighlightingReconciler implements ITextInputListener, IReconcilingListener {

	/**
	 * Collects positions from the AST.
	 */
	public class PositionCollector {

		private final ISemanticHighlighter fHighlighter;

		private PositionCollector(ISemanticHighlighter highlighter) {
			fHighlighter = highlighter;
			fHighlighter.setCollector(this);
		}

		public boolean isEnabled(int highlighting) {
			if (highlighting >= 0 && highlighting < fJobSemanticHighlightings.length) {
				ColorDescriptor semanticHighlighting = fJobSemanticHighlightings[highlighting];
				Highlighting hl = semanticHighlighting.getHighlighting();
				return hl.isEnabled();
			}
			return false;
		}

		public boolean addPosition(int offset, int length, int highlighting) {
			if (highlighting >= 0 && highlighting < fJobSemanticHighlightings.length) {
				ColorDescriptor semanticHighlighting = fJobSemanticHighlightings[highlighting];
				Highlighting hl = semanticHighlighting.getHighlighting();
				if (hl.isEnabled()) {
					if (offset > -1 && length > 0) {
						addPosition(offset, length, hl);
					}
				}
			}
			return false;
		}

		/**
		 * Add a position with the given range and highlighting if it does not
		 * exist already.
		 */
		private void addPosition(int offset, int length, Highlighting highlighting) {
			boolean isExisting = false;
			// TODO: use binary search
			for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position = fRemovedPositions.get(i);
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
				HighlightedPosition position = fJobPresenter.createHighlightedPosition(offset, length, highlighting);
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

		public void enumerate(ISourceStructure model) {
			fHighlighter.highlight(model);
		}
	}

	/** Position collector */
	private PositionCollector fCollector;

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

	public void aboutToBeReconciled() {
		// Do nothing
	}

	public void reconciled(ISourceStructure model, IProgressMonitor progressMonitor) {
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

		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null) {
				return;
			}

			fJobPresenter.setCanceled(progressMonitor.isCanceled());

			if (model == null || fJobPresenter.isCanceled()) {
				return;
			}

			startReconcilingPositions();

			if (!fJobPresenter.isCanceled()) {
				reconcilePositions(model);
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
	private void reconcilePositions(ISourceStructure model) {
		if (!model.hasAst() || fCollector == null) {
			return;
		}
		fCollector.enumerate(model);
		List<HighlightedPosition> oldPositions = fRemovedPositions;
		List<HighlightedPosition> newPositions = new ArrayList<HighlightedPosition>(fNOfRemovedPositions);
		for (int i = 0, n = oldPositions.size(); i < n; i++) {
			HighlightedPosition current = oldPositions.get(i);
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
			SemanticHighlightingPresenter presenter, ColorDescriptor[] semanticHighlightings) {
		fPresenter = presenter;
		fSemanticHighlightings = semanticHighlightings;

		fEditor = editor;
		fSourceViewer = sourceViewer;

		if (fEditor != null) {
			fCollector = new PositionCollector(editor.createSemanticHighlighter());
			fEditor.addReconcileListener(this);
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

		fCollector = null;
		fSourceViewer = null;
		fSemanticHighlightings = null;
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
							LapgCommonActivator.log(e);
							return Status.CANCEL_STATUS;
						}
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					ISourceStructure model = fEditor.getModel();
					reconciled(model, monitor);
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
