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
package org.textmapper.lapg.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.textmapper.lapg.common.ui.editor.IStructuredDocumentProvider;
import org.textmapper.lapg.common.ui.editor.StructuredTextEditor;
import org.textmapper.lapg.ui.structure.LapgSourceStructure;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.ast.*;

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

		TMTree<TmaInput> ast = model.getAst();
		if (ast == null || ast.getRoot() == null) {
			return null;
		}

		ReferenceFinder finder = new ReferenceFinder(offset);
		ast.getRoot().accept(finder);
		ITmaNode ref = finder.getResult();
		if (ref == null) {
			return null;
		}

		Region refregion = new Region(ref.getOffset(), ref.getEndoffset() - ref.getOffset());
		return new IHyperlink[]{new LapgReferenceHyperlink((LapgSourceEditor) textEditor, refregion, model, ref)};
	}

	private static class ReferenceFinder extends TmaVisitor {

		private final int offset;
		private ITmaNode result;

		public ReferenceFinder(int offset) {
			this.offset = offset;
		}

		@Override
		public boolean visit(TmaSymref ref) {
			if (ref.getOffset() <= offset && ref.getEndoffset() >= offset) {
				result = ref;
			}
			return true;
		}

		@Override
		public boolean visit(TmaStateref ref) {
			if (ref.getOffset() <= offset && ref.getEndoffset() >= offset) {
				result = ref;
			}
			return true;
		}

		public ITmaNode getResult() {
			return result;
		}
	}

}
