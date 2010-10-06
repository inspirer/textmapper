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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class StructuredTextViewerConfiguration extends TextSourceViewerConfiguration {

	public static final class StructuredTextPresentationReconciler extends PresentationReconciler {
		/**
		 * Last used document
		 */
		private IDocument fLastDocument;

		/**
		 * Constructs a "repair description" for the given damage and returns
		 * this description as a text presentation.
		 * <p>
		 * NOTE: Should not be used if this reconciler is installed on a viewer.
		 * </p>
		 */
		public TextPresentation createRepairDescription(IRegion damage, IDocument document) {
			if (document != fLastDocument) {
				setDocumentToDamagers(document);
				setDocumentToRepairers(document);
				fLastDocument = document;
			}
			return createPresentation(damage, document);
		}
	}

	public StructuredTextViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}
}
