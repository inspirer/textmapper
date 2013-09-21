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

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.DetectedFacetPresentation;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.TextmapperBundle;
import org.textmapper.idea.lang.syntax.TMFileType;

import javax.swing.*;
import java.util.Collection;

public class TMFacetType extends FacetType<TMFacet, TMFacetConfiguration> {

	public static final FacetTypeId<TMFacet> ID = new FacetTypeId<TMFacet>("textmapper");

	public TMFacetType() {
		super(ID, TmFacetConstants.TM_FACET_ID, TmFacetConstants.TM_FACET_NAME);
	}

	@Override
	public TMFacetConfiguration createDefaultConfiguration() {
		return new TMFacetConfiguration();
	}

	@Override
	public TMFacet createFacet(@NotNull Module module, String name, @NotNull TMFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
		return new TMFacet(this, module, name, configuration, underlyingFacet);
	}

	@Override
	public boolean isSuitableModuleType(ModuleType moduleType) {
		return true;
	}

	@Override
	public Icon getIcon() {
		return TMIcons.TM_ICON;
	}

	@Override
	public void registerDetectors(final FacetDetectorRegistry<TMFacetConfiguration> registry) {
		FacetDetector<VirtualFile, TMFacetConfiguration> detector = new TMFacetDetector();
		final boolean[] detected = new boolean[] { false };

		VirtualFileFilter filter = new VirtualFileFilter() {
			public boolean accept(VirtualFile file) {
				if(detected[0]) return true;
				detected[0] = true;
				if(TMFileType.DEFAULT_EXTENSION.equals(file.getExtension())) {
					registry.customizeDetectedFacetPresentation(new LapgFacetPresentation());
					return true;
				}
				return false;
			}
		};

		registry.registerUniversalDetector(TMFileType.TM_FILE_TYPE, filter, detector);
	}

	private class TMFacetDetector extends FacetDetector<VirtualFile, TMFacetConfiguration> {

		private TMFacetDetector() {
			super("textmapper");
		}

		@Override
		public TMFacetConfiguration detectFacet(VirtualFile source, Collection<TMFacetConfiguration> existentFacetConfigurations) {
			if (!existentFacetConfigurations.isEmpty()) {
			  return existentFacetConfigurations.iterator().next();
			}
			return createDefaultConfiguration();
		}
	}

	private static class LapgFacetPresentation extends DetectedFacetPresentation {
		@Override
		public String getAutodetectionPopupText(@NotNull Module module, @NotNull FacetType facetType, @NotNull String facetName, @NotNull VirtualFile[] files) {
			return TextmapperBundle.message("facet.detected");
		}
	}
}
