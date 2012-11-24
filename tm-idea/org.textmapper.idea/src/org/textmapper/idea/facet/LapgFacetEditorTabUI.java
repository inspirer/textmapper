/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.facet;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;

public class LapgFacetEditorTabUI {

	private JCheckBox noDefaultTemplatesCheckBox;
	private JCheckBox verboseOutputCheckBox;
	private TextFieldWithBrowseButton templatesFolder;
	private JPanel rootComponent;

	private static final FileChooserDescriptor TEMPLATES_CHOOSER_DESCRIPTOR = FileChooserDescriptorFactory.createSingleFolderDescriptor();

	static {
		TEMPLATES_CHOOSER_DESCRIPTOR.setTitle("Choose Textmapper Templates Folder");
		TEMPLATES_CHOOSER_DESCRIPTOR.setDescription("Choose the directory with custom Textmapper templates");
	}

	public LapgFacetEditorTabUI() {
		templatesFolder.addBrowseFolderListener(null, null, null, TEMPLATES_CHOOSER_DESCRIPTOR);
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setData(LapgConfigurationBean data) {
		noDefaultTemplatesCheckBox.setSelected(data.excludeDefaultTemplates);
		verboseOutputCheckBox.setSelected(data.verbose);
		templatesFolder.setText(data.templatesFolder);
	}

	public void getData(LapgConfigurationBean data) {
		data.excludeDefaultTemplates = noDefaultTemplatesCheckBox.isSelected();
		data.verbose = verboseOutputCheckBox.isSelected();
		data.templatesFolder = templatesFolder.getText();
	}

	public boolean isModified(LapgConfigurationBean data) {
		if (noDefaultTemplatesCheckBox.isSelected() != data.excludeDefaultTemplates) return true;
		if (verboseOutputCheckBox.isSelected() != data.verbose) return true;
		if (!templatesFolder.getText().equals(data.templatesFolder)) return true;
		return false;
	}
}
