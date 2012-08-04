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
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.textmapper.lapg.parser.ast.AstGrammarPart;
import org.textmapper.lapg.parser.ast.AstIdentifier;
import org.textmapper.lapg.parser.ast.AstLexeme;
import org.textmapper.lapg.parser.ast.AstLexerPart;
import org.textmapper.lapg.parser.ast.AstNonTerm;
import org.textmapper.lapg.parser.ast.AstReference;
import org.textmapper.lapg.parser.ast.AstRoot;
import org.textmapper.lapg.parser.ast.IAstNode;
import org.textmapper.lapg.ui.structure.LapgSourceStructure;

public class LapgReferenceHyperlink implements IHyperlink {

	private final IRegion fHyperlinkRegion;
	private final LapgSourceStructure fModel;
	private final IAstNode fReference;
	private final LapgSourceEditor fEditor;

	public LapgReferenceHyperlink(LapgSourceEditor editor, IRegion hyperlinkRegion, LapgSourceStructure model, IAstNode reference) {
		fEditor = editor;
		fHyperlinkRegion = hyperlinkRegion;
		fModel = model;
		fReference = reference;
	}

	public IRegion getHyperlinkRegion() {
		return fHyperlinkRegion;
	}

	public String getHyperlinkText() {
		return null;
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		if (fModel == null) {
			return;
		}

		try {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow == null) {
				return;
			}
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage == null) {
				return;
			}
			
			AstReference ref = (AstReference) fReference;
			String symbolName = ref.getName();

			AstRoot root = fModel.getAst().getRoot();
			IAstNode resolved = searchInRoot(root, symbolName);
			if (resolved == null) {
				return;
			}
			
			if(resolved.getInput() == fModel.getAst().getSource()) {
				openLocal(activePage, fModel, resolved);
			} else {
				// TODO
//				activePage.getNavigationHistory().markLocation(activePage.getActiveEditor());
//				if (open(activePage, fModel, resolved)) {
//					activePage.getNavigationHistory().markLocation(activePage.getActiveEditor());
//				}
			}
		} catch (PartInitException e) {
			// ignored
		}
	}

	private void openLocal(IWorkbenchPage activePage, LapgSourceStructure model, IAstNode resolved)
			throws PartInitException {

		AstIdentifier identifier = null;
		if(resolved instanceof AstNonTerm) {
			identifier = ((AstNonTerm) resolved).getName();
		} else if(resolved instanceof AstLexeme) {
			identifier = ((AstLexeme) resolved).getName();
		}

		int start = resolved.getOffset();
		fEditor.reveal(start, 
				identifier != null ? identifier.getOffset() : start,
				identifier != null ? identifier.getEndOffset() : start,
				resolved.getEndOffset());				
	}

	private IAstNode searchInRoot(AstRoot root, String symbolName) {
		for(AstGrammarPart p : root.getGrammar()) {
			if(p instanceof AstNonTerm) {
				if(((AstNonTerm)p).getName().getName().equals(symbolName)) {
					return p;
				}
			}			
		}
		for(AstLexerPart p : root.getLexer()) {
			if(p instanceof AstLexeme) {
				if(((AstLexeme)p).getName().getName().equals(symbolName)) {
					return p;
				}
			}			
		}
		if(symbolName.endsWith("opt")) {
			return searchInRoot(root, symbolName.substring(0, symbolName.length() - 3));
		}
		return null;
	}
}
