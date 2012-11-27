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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.module.JpsModule;
import org.textmapper.idea.facet.TmConfigurationBean;
import org.textmapper.jps.model.JpsTmModuleExtension;

/**
 * evgeny, 11/26/12
 */
public class JpsTmModuleExtensionImpl extends JpsCompositeElementBase<JpsTmModuleExtensionImpl> implements JpsTmModuleExtension {

	public static final JpsElementChildRole<JpsTmModuleExtension> ROLE = JpsElementChildRoleBase.create("Textmapper");

	private final TmConfigurationBean myProperties;

	public JpsTmModuleExtensionImpl(TmConfigurationBean myProperties) {
		this.myProperties = myProperties;
	}

	public TmConfigurationBean getProperties() {
		return myProperties;
	}

	@NotNull
	@Override
	public BulkModificationSupport<?> getBulkModificationSupport() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public JpsTmModuleExtensionImpl createCopy() {
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public JpsModule getModule() {
		return (JpsModule) myParent;
	}

	@Override
	public boolean isVerbose() {
		return myProperties.verbose;
	}

	@Override
	public boolean isExcludeDefaultTemplates() {
		return myProperties.excludeDefaultTemplates;
	}

	@Override
	public String getCustomTemplatesFolder() {
		return myProperties.templatesFolder;
	}
}
