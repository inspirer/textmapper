/**
 * Copyright (c) 2010-2011 Evgeny Gryaznov
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
package org.textway.lapg.idea.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.textway.lapg.idea.LapgIcons;

import javax.swing.*;

public class LapgFacetConfiguration implements FacetConfiguration, PersistentStateComponent<LapgConfigurationBean> {

	private LapgConfigurationBean lapgConfigurationBean = new LapgConfigurationBean();

	public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
		return new FacetEditorTab[] { new LapgFacetEditorTab() };
	}

	@SuppressWarnings({"deprecation"})
	public void readExternal(Element element) throws InvalidDataException {
		// ignore
	}

	@SuppressWarnings({"deprecation"})
	public void writeExternal(Element element) throws WriteExternalException {
		// ignore
	}

	public LapgConfigurationBean getState() {
		return lapgConfigurationBean;
	}

	public void loadState(LapgConfigurationBean state) {
		XmlSerializerUtil.copyBean(state, lapgConfigurationBean);
	}

	public class LapgFacetEditorTab extends FacetEditorTab {

		private LapgFacetEditorTabUI form;

		@Override
		public Icon getIcon() {
			return LapgIcons.LAPG_ICON;
		}

		@Nls
		public String getDisplayName() {
			return "Lapg";
		}

		public JComponent createComponent() {
			if(form == null) {
				form = new LapgFacetEditorTabUI();
			}
			return form.getRootComponent();
		}

		public boolean isModified() {
			return form != null && form.isModified(lapgConfigurationBean);
		}

		@Override
		public void apply() throws ConfigurationException {
			if(form != null) {
				form.getData(lapgConfigurationBean);
			}
		}

		public void reset() {
			if(form != null) {
				form.setData(lapgConfigurationBean);
			}
		}

		public void disposeUIResources() {
			form = null;
		}
	}
}
