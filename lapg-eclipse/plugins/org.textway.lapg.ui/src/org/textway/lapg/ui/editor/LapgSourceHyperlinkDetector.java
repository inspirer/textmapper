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
package org.textway.lapg.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.textway.lapg.common.ui.editor.IStructuredDocumentProvider;
import org.textway.lapg.common.ui.editor.StructuredTextEditor;
import org.textway.lapg.common.ui.editor.StructuredTextReconciler.IReconcilingListener;
import org.textway.lapg.parser.LapgTree;
import org.textway.lapg.parser.ast.AbstractVisitor;
import org.textway.lapg.parser.ast.AstReference;
import org.textway.lapg.parser.ast.AstRoot;
import org.textway.lapg.parser.ast.IAstNode;
import org.textway.lapg.ui.structure.LapgSourceStructure;

public class LapgSourceHyperlinkDetector extends AbstractHyperlinkDetector {

	public LapgSourceHyperlinkDetector(StructuredTextEditor context) {
		setContext(context);
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		ITextEditor textEditor = (ITextEditor) getAdapter(ITextEditor.class);
		if (region == null || !(textEditor instanceof LapgSourceEditor)) {
			return null;
		}

		int offset = region.getOffset();

		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		if (!(documentProvider instanceof IStructuredDocumentProvider)) {
			return null;
		}

		LapgSourceStructure model = (LapgSourceStructure) ((IStructuredDocumentProvider) documentProvider)
				.getStructure();
		if (model == null) {
			return null;
		}

		LapgTree<AstRoot> ast = model.getAst();
		if (ast == null || ast.getRoot() == null) {
			return null;
		}

		ReferenceFinder finder = new ReferenceFinder(offset);
		ast.getRoot().accept(finder);
		IAstNode ref = finder.getResult();
		if (ref == null) {
			return null;
		}

		Region refregion = new Region(ref.getOffset(), ref.getEndOffset() - ref.getOffset());
		return new IHyperlink[] { new LapgReferenceHyperlink((LapgSourceEditor) textEditor, refregion, model, ref) };
	}

	private static class ReferenceFinder extends AbstractVisitor {

		private final int offset;
		private IAstNode result;

		public ReferenceFinder(int offset) {
			this.offset = offset;
		}

		@Override
		public boolean visit(AstReference ref) {
			if (ref.getOffset() <= offset && ref.getEndOffset() >= offset) {
				result = ref;
			}
			return true;
		}

		public IAstNode getResult() {
			return result;
		}
	}

}
