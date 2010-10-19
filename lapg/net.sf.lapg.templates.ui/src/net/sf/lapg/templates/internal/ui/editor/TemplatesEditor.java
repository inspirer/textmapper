package net.sf.lapg.templates.internal.ui.editor;

import net.sf.lapg.templates.internal.ui.Activator;

import org.eclipse.ui.editors.text.TextEditor;

public class TemplatesEditor extends TextEditor {

	private ColorManager colorManager;

	public TemplatesEditor() {
		super();
		colorManager = new ColorManager(Activator.getDefault().getPreferenceStore());
		colorManager.initializeDefaults();
		setSourceViewerConfiguration(new TemplatesConfiguration(colorManager));
		setDocumentProvider(new TemplatesDocumentProvider());
	}

	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
