package net.sf.lapg.ui.editor.colorer;

import net.sf.lapg.common.ui.editor.ISourceStructure;
import net.sf.lapg.common.ui.editor.colorer.ISemanticHighlighter;
import net.sf.lapg.common.ui.editor.colorer.SemanticHighlightingReconciler.PositionCollector;
import org.textway.lapg.parser.ast.AbstractVisitor;
import org.textway.lapg.parser.ast.AstError;
import org.textway.lapg.parser.ast.AstIdentifier;
import org.textway.lapg.parser.ast.AstNode;
import org.textway.lapg.parser.ast.AstReference;
import net.sf.lapg.ui.structure.LapgSourceStructure;

public class LapgSemanticHighlighter extends AbstractVisitor implements ISemanticHighlighter {

	protected static final int INDEX_CLASSID = 0;
	protected static final int INDEX_REFERENCE = 1;

	protected PositionCollector fCollector;

	protected LapgSourceStructure fModel;

	public void setModel(ISourceStructure model) {
		fModel = (LapgSourceStructure) model;
	}

	@Override
	public boolean visit(AstError node) {
		int start = node.getOffset();
		int end = node.getEndOffset();
		fCollector.retainPositions(start, end - start);
		return false;
	}

	@Override
	public boolean visit(AstIdentifier n) {
		return visitToken(n, INDEX_CLASSID);
	}

	@Override
	public boolean visit(AstReference n) {
		return visitToken(n, INDEX_REFERENCE);
	}
	
	public boolean visitToken(AstNode node, int highlighting) {
		int offset = node.getOffset();
		int length = node.getEndOffset() - offset;
		if (offset > -1 && length > 0) {
			fCollector.addPosition(offset, length, highlighting);
		}
		return false;
	}

	@Override
	public void setCollector(PositionCollector positionCollector) {
		fCollector = positionCollector;
	}

	@Override
	public void highlight(ISourceStructure model) {
		try {
			setModel(model);
			((LapgSourceStructure)model).accept(this);
		} finally {
			setModel(null);
		}
	}
}
