package org.textmapper.lapg.ui.editor.colorer;

import org.textmapper.lapg.common.ui.editor.ISourceStructure;
import org.textmapper.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import org.textmapper.lapg.common.ui.editor.colorer.SemanticHighlightingReconciler.PositionCollector;
import org.textmapper.lapg.ui.structure.LapgSourceStructure;
import org.textmapper.tool.parser.ast.TmaVisitor;
import org.textmapper.tool.parser.ast.TmaSyntaxProblem;
import org.textmapper.tool.parser.ast.TmaIdentifier;
import org.textmapper.tool.parser.ast.TmaNode;
import org.textmapper.tool.parser.ast.TmaSymref;

public class LapgSemanticHighlighter extends TmaVisitor implements ISemanticHighlighter {

	protected static final int INDEX_CLASSID = 0;
	protected static final int INDEX_REFERENCE = 1;

	protected PositionCollector fCollector;

	protected LapgSourceStructure fModel;

	public void setModel(ISourceStructure model) {
		fModel = (LapgSourceStructure) model;
	}

	@Override
	public boolean visit(TmaSyntaxProblem node) {
		int start = node.getOffset();
		int end = node.getEndOffset();
		fCollector.retainPositions(start, end - start);
		return false;
	}

	@Override
	public boolean visit(TmaIdentifier n) {
		return visitToken(n, INDEX_CLASSID);
	}

	@Override
	public boolean visit(TmaSymref n) {
		return visitToken(n, INDEX_REFERENCE);
	}
	
	public boolean visitToken(TmaNode node, int highlighting) {
		int offset = node.getOffset();
		int length = node.getEndOffset() - offset;
		if (offset > -1 && length > 0) {
			fCollector.addPosition(offset, length, highlighting);
		}
		return false;
	}

	public void setCollector(PositionCollector positionCollector) {
		fCollector = positionCollector;
	}

	public void highlight(ISourceStructure model) {
		try {
			setModel(model);
			((LapgSourceStructure)model).accept(this);
		} finally {
			setModel(null);
		}
	}
}
