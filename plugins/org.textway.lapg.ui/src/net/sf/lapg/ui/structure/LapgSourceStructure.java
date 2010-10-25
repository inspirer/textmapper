package net.sf.lapg.ui.structure;

import java.util.ArrayList;
import java.util.List;

import org.textway.lapg.api.Grammar;
import net.sf.lapg.common.ui.editor.ISourceStructure;
import net.sf.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import org.textway.lapg.parser.LapgTree;
import org.textway.lapg.parser.LapgTree.LapgProblem;
import org.textway.lapg.parser.ast.AbstractVisitor;
import org.textway.lapg.parser.ast.AstRoot;
import net.sf.lapg.ui.LapgUIActivator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

public class LapgSourceStructure implements ISourceStructure {

	private final IFile file;    // can be null
	private final LapgTree<AstRoot> ast;
	private final Grammar grammar;

	public LapgSourceStructure(Grammar grammar, LapgTree<AstRoot> ast, IFile file) {
		this.grammar = grammar;
		this.ast = ast;
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	public LapgTree<AstRoot> getAst() {
		return ast;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public List<LapgProblem> getProblems() {
		return ast.getErrors();
	}

	public IStatus getStatus() {
		List<IStatus> errors = new ArrayList<IStatus>(ast.getErrors().size());
		for (LapgProblem p : ast.getErrors()) {
			switch (p.getKind()) {
			case LapgTree.KIND_FATAL:
			case LapgTree.KIND_ERROR:
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
		getAst().getRoot().accept((AbstractVisitor) fHighlighter);
	}
}
