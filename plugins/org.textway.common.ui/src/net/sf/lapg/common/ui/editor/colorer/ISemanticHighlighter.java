package net.sf.lapg.common.ui.editor.colorer;

import net.sf.lapg.common.ui.editor.ISourceStructure;
import net.sf.lapg.common.ui.editor.colorer.SemanticHighlightingReconciler.PositionCollector;

public interface ISemanticHighlighter {

	void setCollector(PositionCollector positionCollector);

	void highlight(ISourceStructure model);
}
