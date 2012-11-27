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
package org.textmapper.jps.model.impl;

import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.facet.JpsFacetConfigurationSerializer;
import org.textmapper.idea.facet.TmConfigurationBean;
import org.textmapper.idea.facet.TmFacetConstants;
import org.textmapper.jps.model.JpsTmModuleExtension;

import java.util.Collections;
import java.util.List;

/**
 * evgeny, 11/26/12
 */
public class JpsTmModelSerializerExtension extends JpsModelSerializerExtension {

	@Override
	public List<? extends JpsFacetConfigurationSerializer<?>> getFacetConfigurationSerializers() {
		return Collections.singletonList(new JpsTmFacetConfigurationSerializer());
	}

	private static class JpsTmFacetConfigurationSerializer extends JpsFacetConfigurationSerializer<JpsTmModuleExtension> {
		public JpsTmFacetConfigurationSerializer() {
			super(JpsTmModuleExtensionImpl.ROLE, TmFacetConstants.TM_FACET_ID, TmFacetConstants.TM_FACET_NAME);
		}

		@Override
		protected JpsTmModuleExtension loadExtension(@NotNull Element facetConfigurationElement,
													 String name,
													 JpsElement parent,
													 JpsModule module) {
			TmConfigurationBean properties = XmlSerializer.deserialize(facetConfigurationElement, TmConfigurationBean.class);
			return new JpsTmModuleExtensionImpl(properties != null ? properties : new TmConfigurationBean());
		}

		@Override
		protected void saveExtension(JpsTmModuleExtension extension, Element facetConfigurationTag, JpsModule module) {
			XmlSerializer.serializeInto(((JpsTmModuleExtensionImpl) extension).getProperties(), facetConfigurationTag, new SkipDefaultValuesSerializationFilters());
		}
	}
}
