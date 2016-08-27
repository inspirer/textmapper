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
package org.textmapper.jps.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.service.JpsServiceManager;

/**
 * evgeny, 11/27/12
 */
public abstract class JpsTmExtensionService {

	public static JpsTmExtensionService getInstance() {
		return JpsServiceManager.getInstance().getService(JpsTmExtensionService.class);
	}

	@Nullable
	public abstract JpsTmModuleExtension getExtension(@Nullable JpsModule module);

	public abstract void setExtension(@NotNull JpsModule module, @NotNull JpsTmModuleExtension extension);

}
