/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;

public class TmSettingsConfigurableProvider extends ConfigurableProvider {
	private final Project myProject;

	public TmSettingsConfigurableProvider(Project project) {
		myProject = project;
	}

	@NotNull
	@Override
	public Configurable createConfigurable() {
		return new TmSettingsConfigurable(TmProjectSettings.getInstance(myProject).tmConfigurationBean);
	}

	@Override
	public boolean canCreateConfigurable() {
		// In Community/Ultimate we use module facets instead.
		return !PlatformUtils.isIntelliJ();
	}
}

