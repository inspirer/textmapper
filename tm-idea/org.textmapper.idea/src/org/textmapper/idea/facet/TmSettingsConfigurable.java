/**
 * Copyright 2010-2017 Evgeny Gryaznov
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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TmSettingsConfigurable implements SearchableConfigurable {

	private TmConfigurationBean tmConfigurationBean;
	private TmSettingsEditorUI form;

	public TmSettingsConfigurable(TmConfigurationBean bean) {
		tmConfigurationBean = bean;
	}

	@Nls
	public String getDisplayName() {
		return "Textmapper";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@NotNull
	@Override
	public String getId() {
		return "settings.Textmapper";
	}

	@Nullable
	@Override
	public Runnable enableSearch(String option) {
		return null;
	}

	public JComponent createComponent() {
		if (form == null) {
			form = new TmSettingsEditorUI();
		}
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form != null && form.isModified(tmConfigurationBean);
	}

	@Override
	public void apply() throws ConfigurationException {
		if (form != null) {
			form.getData(tmConfigurationBean);
		}
	}

	public void reset() {
		if (form != null) {
			form.setData(tmConfigurationBean);
		}
	}

	public void disposeUIResources() {
		form = null;
	}

}
