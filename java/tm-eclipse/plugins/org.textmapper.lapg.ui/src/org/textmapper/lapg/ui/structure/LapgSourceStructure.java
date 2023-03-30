package org.textmapper.lapg.ui.structure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.common.ui.editor.ISourceStructure;
import org.textmapper.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import org.textmapper.lapg.ui.LapgUIActivator;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.ast.TmaVisitor;
import org.textmapper.tool.parser.ast.TmaInput;

public class LapgSourceStructure implements ISourceStructure {

	private final IFile file;    // can be null
	private final TMTree<TmaInput> ast;
	private final Grammar grammar;

	public LapgSourceStructure(Grammar grammar, TMTree<TmaInput> ast, IFile file) {
		this.grammar = grammar;
		this.ast = ast;
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	public TMTree<TmaInput> getAst() {
		return ast;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public List<TMProblem> getProblems() {
		return ast.getErrors();
	}

	public IStatus getStatus() {
		List<IStatus> errors = new ArrayList<IStatus>(ast.getErrors().size());
		for (TMProblem p : ast.getErrors()) {
			switch (p.getKind()) {
				case TMTree.KIND_FATAL:
				case TMTree.KIND_ERROR:
					errors.add(new Status(IStatus.ERROR, LapgUIActivator.PLUGIN_ID, 0, p.getMessage(), null));
					break;
			}
		}
		if (errors.size() > 0) {
			return new MultiStatus(LapgUIActivator.PLUGIN_ID, 0, errors.toArray(new IStatus[errors.size()]),
					"Problems in text", null);
		}
		return Status.OK_STATUS;
	}

	public boolean hasAst() {
		return getAst().getRoot() != null;
	}

	public void accept(ISemanticHighlighter fHighlighter) {
		getAst().getRoot().accept((TmaVisitor) fHighlighter);
	}
}
