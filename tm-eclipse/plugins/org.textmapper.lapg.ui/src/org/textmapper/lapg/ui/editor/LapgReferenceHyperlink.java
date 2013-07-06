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
import org.textmapper.lapg.ui.structure.LapgSourceStructure;
import org.textmapper.tool.parser.ast.*;

public class LapgReferenceHyperlink implements IHyperlink {

    private final IRegion fHyperlinkRegion;
    private final LapgSourceStructure fModel;
    private final ITmaNode fReference;
    private final LapgSourceEditor fEditor;

    public LapgReferenceHyperlink(LapgSourceEditor editor, IRegion hyperlinkRegion, LapgSourceStructure model, ITmaNode reference) {
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

    private String getName() {
        if (fReference instanceof TmaSymref) {
            return ((TmaSymref) fReference).getName();
        } else if (fReference instanceof TmaStateref) {
            return ((TmaStateref) fReference).getName();
        }
        throw new IllegalStateException();
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

            String symbolName = getName();

            TmaInput root = fModel.getAst().getRoot();
            ITmaNode resolved = searchInRoot(root, symbolName);
            if (resolved == null) {
                return;
            }

            if (resolved.getInput() == fModel.getAst().getSource()) {
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

    private void openLocal(IWorkbenchPage activePage, LapgSourceStructure model, ITmaNode resolved)
            throws PartInitException {

        TmaIdentifier identifier = null;
        if (resolved instanceof TmaNonterm) {
            identifier = ((TmaNonterm) resolved).getName();
        } else if (resolved instanceof TmaLexeme) {
            identifier = ((TmaLexeme) resolved).getName();
        }

        int start = resolved.getOffset();
        fEditor.reveal(start,
                identifier != null ? identifier.getOffset() : start,
                identifier != null ? identifier.getEndOffset() : start,
                resolved.getEndOffset());
    }

    private ITmaNode searchInRoot(TmaInput root, String symbolName) {
        for (TmaGrammarPart p : root.getGrammar()) {
            if (p instanceof TmaNonterm) {
                if (((TmaNonterm) p).getName().getID().equals(symbolName)) {
                    return p;
                }
            }
        }
        for (TmaLexerPart p : root.getLexer()) {
            if (p instanceof TmaLexeme) {
                if (((TmaLexeme) p).getName().getID().equals(symbolName)) {
                    return p;
                }
            }
        }
        if (symbolName.endsWith("opt")) {
            return searchInRoot(root, symbolName.substring(0, symbolName.length() - 3));
        }
        return null;
    }
}
