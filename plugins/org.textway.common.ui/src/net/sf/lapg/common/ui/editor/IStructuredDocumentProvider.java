package net.sf.lapg.common.ui.editor;

import org.eclipse.ui.texteditor.IDocumentProvider;


public interface IStructuredDocumentProvider extends IDocumentProvider {
	public void setStructure(ISourceStructure model);

	public ISourceStructure getStructure();
}
