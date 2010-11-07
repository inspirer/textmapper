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
package net.sf.lapg.common.ui.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.StructuredTextReconciler.IReconcilingListener;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import net.sf.lapg.common.ui.editor.colorer.SemanticHighlightingManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public abstract class StructuredTextEditor extends TextEditor {

	private DefaultHighlightingManager fHighlightingManager;
	private SemanticHighlightingManager fSemanticManager;

	public StructuredTextEditor() {
		setDocumentProvider(createDocumentProvider());
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer = getSourceViewer();
		if (!(sourceViewer instanceof ISourceViewerExtension2)) {
			setPreferenceStore(createCombinedPreferenceStore(input));
			internalDoSetInput(input);
			return;
		}

		// uninstall & unregister preference store listener
		getSourceViewerDecorationSupport(sourceViewer).uninstall();
		((ISourceViewerExtension2) sourceViewer).unconfigure();

		setPreferenceStore(createCombinedPreferenceStore(input));

		// install & register preference store listener
		sourceViewer.configure(getSourceViewerConfiguration());
		getSourceViewerDecorationSupport(sourceViewer).install(getPreferenceStore());

		internalDoSetInput(input);
	}

	private void internalDoSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		StructuredTextViewer sourceViewer = null;
		if (getSourceViewer() instanceof StructuredTextViewer) {
			sourceViewer = (StructuredTextViewer) getSourceViewer();
		}

		if (sourceViewer != null && sourceViewer.getReconciler() == null) {
			IReconciler reconciler = getSourceViewerConfiguration().getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				sourceViewer.setReconciler(reconciler);
			}
		}
	}

	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>(2);
		stores.add(getPluginPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());
		return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
	}

	@Override
	protected void initializeEditor() {
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	@Override
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		setSourceViewerConfiguration(createSourceViewerConfiguration());
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		try {
			if (fHighlightingManager != null) {
				fHighlightingManager.propertyChange(event);
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}


	@Override
	public void createPartControl(Composite parent) {
		fHighlightingManager = createHighlightingManager();
		super.createPartControl(parent);

		createStructureProvider();
		installSemanticHighlighting();
	}

	private void installSemanticHighlighting() {
		if (fSemanticManager == null) {
			fSemanticManager = new SemanticHighlightingManager();
			fSemanticManager.install(this, (StructuredTextViewer) getSourceViewer(), getHighlightingManager(), getPreferenceStore());
		}
	}

	@Override
	public void dispose() {
		uninstallSemanticHighlighting();
		if (fStructureProvider != null) {
			fStructureProvider.dispose();
		}
		if (fHighlightingManager != null) {
			fHighlightingManager.dispose();
			fHighlightingManager = null;
		}
		super.dispose();
	}

	private void uninstallSemanticHighlighting() {
		if (fSemanticManager != null) {
			fSemanticManager.uninstall();
			fSemanticManager = null;
		}
	}

	private final ListenerList fReconcilingListeners = new ListenerList(ListenerList.IDENTITY);

	public void aboutToBeReconciled() {
		waitStructureProvider();

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length = listeners.length; i < length; ++i) {
			((IReconcilingListener) listeners[i]).aboutToBeReconciled();
		}
	}

	public void reconciled(ISourceStructure model, IProgressMonitor progressMonitor) {
		waitStructureProvider();

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length = listeners.length; i < length; ++i) {
			((IReconcilingListener) listeners[i]).reconciled(model, progressMonitor);
		}
	}

	public final void addReconcileListener(IReconcilingListener listener) {
		fReconcilingListeners.add(listener);
	}

	public final void removeReconcileListener(IReconcilingListener listener) {
		fReconcilingListeners.remove(listener);
	}

	public DefaultHighlightingManager getHighlightingManager() {
		return fHighlightingManager;
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		if (fHighlightingManager != null) {
			if (fHighlightingManager.isAffected(event)) {
				return true;
			}
		}
		return super.affectsTextPresentation(event);
	}

	public StructuredTextViewerConfiguration createSourceViewerConfiguration() {
		return new StructuredTextViewerConfiguration(getPreferenceStore());
	}

	protected IStructuredDocumentProvider createDocumentProvider() {
		return new StructuredDocumentProvider();
	}

	public abstract ISemanticHighlighter createSemanticHighlighter();

	protected abstract DefaultHighlightingManager createHighlightingManager();

	/**
	 * return YourPluginActivator.getInstance().getPreferenceStore()
	 */
	protected abstract IPreferenceStore getPluginPreferenceStore();

	private StructureProvider fStructureProvider;
	private final Object fProviderLock = new Object();

	private void createStructureProvider() {
		synchronized (fProviderLock) {
			fStructureProvider = new StructureProvider();
			// notify possible waiting clients
			fProviderLock.notifyAll();
		}
	}

	private void waitStructureProvider() {
		synchronized (fProviderLock) {
			while (fStructureProvider == null) {
				try {
					fProviderLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	public ISourceStructure getModel(long timeoutInMilisec) {
		waitStructureProvider();
		return fStructureProvider.getModel(timeoutInMilisec);
	}

	private class StructureProvider implements IReconcilingListener {

		private final IDocumentListener fDocListener;
		private boolean fNeedsReconciling = true;
		private long fModifyTimeStamp = 0;
		private long fStartReconcileTimeStamp = 0;
		private final Object fLock = new Object();

		public StructureProvider() {
			IDocument doc = getDocument();
			if (doc == null) {
				throw new IllegalStateException("Editor source viewer document must be available"); //$NON-NLS-1$
			}
			addReconcileListener(this);

			fDocListener = new IDocumentListener() {
				public void documentAboutToBeChanged(DocumentEvent event) {
					synchronized (fLock) {
						fNeedsReconciling = true;
						fModifyTimeStamp = event.fModificationStamp;
					}
				}

				public void documentChanged(DocumentEvent event) {
					// do nothing
				}
			};

			doc.addDocumentListener(fDocListener);
		}

		public ISourceStructure getModel(long timeoutInMilisec) {
			IStructuredDocumentProvider documentProvider = (IStructuredDocumentProvider) getDocumentProvider();
			synchronized (fLock) {
				while (fNeedsReconciling) {
					try {
						fLock.wait(timeoutInMilisec);
						if (fNeedsReconciling) {
							// time-outed
							return null;
						}
					} catch (InterruptedException e) {
						return null;
					}
				}
				return documentProvider.getStructure();
			}
		}

		public void aboutToBeReconciled() {
			synchronized (fLock) {
				fStartReconcileTimeStamp = fModifyTimeStamp;
			}
		}

		public void reconciled(ISourceStructure model, IProgressMonitor progressMonitor) {
			synchronized (fLock) {
				if (fModifyTimeStamp == fStartReconcileTimeStamp) {
					fNeedsReconciling = false;
				}
				// wake-up clients waiting for Structure
				fLock.notifyAll();
			}
		}

		private IDocument getDocument() {
			ISourceViewer viewer = getSourceViewer();
			if (viewer != null) {
				return viewer.getDocument();
			}
			return null;
		}

		void dispose() {
			IDocument doc = getDocument();
			if (doc != null) {
				doc.removeDocumentListener(fDocListener);
			}
			removeReconcileListener(this);
		}
	}

	public static class StructuredDocumentProvider extends TextFileDocumentProvider implements IStructuredDocumentProvider {

		private ISourceStructure fModel;

		@Override
		protected IAnnotationModel createAnnotationModel(IFile file) {
			return new ResourceMarkerAnnotationModel(file);
		}

		public void setStructure(ISourceStructure model) {
			fModel = model;
		}

		public ISourceStructure getStructure() {
			return fModel;
		}
	}
}

