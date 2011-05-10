/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.facet;

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
		TEMPLATES_CHOOSER_DESCRIPTOR.setTitle("Choose Lapg Templates folder");
		TEMPLATES_CHOOSER_DESCRIPTOR.setDescription("Choose the directory with custom Lapg templates");
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
