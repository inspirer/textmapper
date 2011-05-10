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

	public void readExternal(Element element) throws InvalidDataException {
		// ignore
	}

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
