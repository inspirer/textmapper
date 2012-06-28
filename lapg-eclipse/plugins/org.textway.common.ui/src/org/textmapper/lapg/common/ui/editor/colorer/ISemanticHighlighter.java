package org.textmapper.lapg.common.ui.editor.colorer;

import org.textmapper.lapg.common.ui.editor.ISourceStructure;
import org.textmapper.lapg.common.ui.editor.colorer.SemanticHighlightingReconciler.PositionCollector;

public interface ISemanticHighlighter {

	void setCollector(PositionCollector positionCollector);

	void highlight(ISourceStructure model);
}
