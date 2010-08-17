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
package net.sf.lapg.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class LapgReconciler extends MonoReconciler {

	public interface IReconcilingListener {
		/**
		 * Called before reconciling is started
		 */
		void aboutToBeReconciled();

		/**
		 * Called after reconciling has been finished.
		 */
		void reconciled(IProgressMonitor progressMonitor);
	}

	private class PartListener implements IPartListener {
		public void partActivated(final IWorkbenchPart part) {
			if (part == fEditor) {
				LapgReconciler.this.forceReconciling();
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	}

	public LapgReconciler(final ITextEditor textEditor, final IReconcilingStrategy strategy,
			final boolean isIncremental) {
		super(strategy, isIncremental);
		fEditor = textEditor;
	}

	void performReconciling() {
		super.forceReconciling();
	}

	@Override
	public void install(final ITextViewer textViewer) {
		super.install(textViewer);

		fPartListener = new PartListener();
		IWorkbenchPartSite site = fEditor.getSite();
		IWorkbenchWindow window = site.getWorkbenchWindow();
		window.getPartService().addPartListener(fPartListener);
	}

	@Override
	public void uninstall() {
		IWorkbenchPartSite site = fEditor.getSite();
		IWorkbenchWindow window = site.getWorkbenchWindow();
		window.getPartService().removePartListener(fPartListener);
		fPartListener = null;

		super.uninstall();
	}

	private final ITextEditor fEditor;

	private PartListener fPartListener;
}
