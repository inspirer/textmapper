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
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TmSettingsEditorUI {

	private JBCheckBox verboseOutputCheckBox = new JBCheckBox("Verbose compiler output");
	private JBCheckBox noDefaultTemplatesCheckBox = new JBCheckBox("Do not use default templates");
	private JBCheckBox useCustomTemplates = new JBCheckBox("Use custom templates:");
	private TextFieldWithBrowseButton customTemplatesDir = new TextFieldWithBrowseButton();
	private JPanel mainPanel = new JPanel(new GridBagLayout());

	private static final FileChooserDescriptor TEMPLATES_CHOOSER_DESCRIPTOR =
			FileChooserDescriptorFactory.createSingleFolderDescriptor();

	static {
		TEMPLATES_CHOOSER_DESCRIPTOR.setTitle("Choose Custom Templates Directory");
		TEMPLATES_CHOOSER_DESCRIPTOR.setDescription("Choose the directory with custom Textmapper templates");
	}

	public TmSettingsEditorUI() {
		customTemplatesDir.addBrowseFolderListener(null, null, null, TEMPLATES_CHOOSER_DESCRIPTOR);
		customTemplatesDir.setEnabled(useCustomTemplates.isSelected());
		useCustomTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final boolean selected = useCustomTemplates.isSelected();
				customTemplatesDir.setEnabled(selected);
			}
		});

		final GridBagConstraints gc = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
		mainPanel.add(verboseOutputCheckBox, gc);
		JPanel templatesPanel = new JPanel(new GridBagLayout());
		templatesPanel.setBorder(IdeBorderFactory.createTitledBorder("Templates settings", true));
		gc.insets.left = 0;
		templatesPanel.add(noDefaultTemplatesCheckBox, gc);
		templatesPanel.add(useCustomTemplates, gc);
		gc.insets.left = 5;
		templatesPanel.add(customTemplatesDir, gc);
		gc.weighty = 1.0;
		final JLabel note = new JLabel(
				"Custom templates take precedence over built-in templates when both are enabled.");
		note.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
		note.setBorder(IdeBorderFactory.createEmptyBorder(0, 20, 0, 0));
		templatesPanel.add(note, gc);
		mainPanel.add(templatesPanel, gc);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	public JComponent getRootComponent() {
		return mainPanel;
	}

	public void setData(TmConfigurationBean data) {
		noDefaultTemplatesCheckBox.setSelected(data.excludeDefaultTemplates);
		verboseOutputCheckBox.setSelected(data.verbose);
		customTemplatesDir.setText(data.templatesFolder);
		useCustomTemplates.setSelected(!data.templatesFolder.isEmpty());
		customTemplatesDir.setEnabled(useCustomTemplates.isSelected());
	}

	public void getData(TmConfigurationBean data) {
		data.excludeDefaultTemplates = noDefaultTemplatesCheckBox.isSelected();
		data.verbose = verboseOutputCheckBox.isSelected();
		data.templatesFolder = useCustomTemplates.isSelected() ? customTemplatesDir.getText() : "";
	}

	public boolean isModified(TmConfigurationBean data) {
		if (noDefaultTemplatesCheckBox.isSelected() != data.excludeDefaultTemplates) return true;
		if (verboseOutputCheckBox.isSelected() != data.verbose) return true;
		String templatesFolderText = useCustomTemplates.isSelected() ? customTemplatesDir.getText() : "";
		if (!templatesFolderText.equals(data.templatesFolder)) return true;
		return false;
	}
}
