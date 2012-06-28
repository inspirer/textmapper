package org.textway.lapg.common.ui.editor.colorer;

import org.textway.lapg.common.ui.editor.ISourceStructure;
import org.textway.lapg.common.ui.editor.colorer.SemanticHighlightingReconciler.PositionCollector;

public interface ISemanticHighlighter {

	void setCollector(PositionCollector positionCollector);

	void highlight(ISourceStructure model);
}
