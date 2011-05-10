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
import org.textway.lapg.idea.LapgBundle;
import org.textway.lapg.idea.LapgIcons;
import org.textway.lapg.idea.file.LapgFileType;

import javax.swing.*;
import java.util.Collection;

public class LapgFacetType extends FacetType<LapgFacet, LapgFacetConfiguration> {

	public static final FacetTypeId<LapgFacet> ID = new FacetTypeId<LapgFacet>("lapg");

	public LapgFacetType() {
		super(ID, "Lapg", "Lapg");
	}

	@Override
	public LapgFacetConfiguration createDefaultConfiguration() {
		return new LapgFacetConfiguration();
	}

	@Override
	public LapgFacet createFacet(@NotNull Module module, String name, @NotNull LapgFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
		return new LapgFacet(this, module, name, configuration, underlyingFacet);
	}

	@Override
	public boolean isSuitableModuleType(ModuleType moduleType) {
		return true;
	}

	@Override
	public Icon getIcon() {
		return LapgIcons.LAPG_ICON;
	}

	@Override
	public void registerDetectors(final FacetDetectorRegistry<LapgFacetConfiguration> registry) {
		FacetDetector<VirtualFile, LapgFacetConfiguration> detector = new LapgFacetDetector();
		final boolean[] detected = new boolean[] { false };

		VirtualFileFilter filter = new VirtualFileFilter() {
			public boolean accept(VirtualFile file) {
				if(detected[0]) return true;
				detected[0] = true;
				if(LapgFileType.DEFAULT_EXTENSION.equals(file.getExtension())) {
					registry.customizeDetectedFacetPresentation(new LapgFacetPresentation());
					return true;
				}
				return false;
			}
		};

		registry.registerUniversalDetector(LapgFileType.LAPG_FILE_TYPE, filter, detector);
	}

	private class LapgFacetDetector extends FacetDetector<VirtualFile, LapgFacetConfiguration> {

		private LapgFacetDetector() {
			super("lapg");
		}

		@Override
		public LapgFacetConfiguration detectFacet(VirtualFile source, Collection<LapgFacetConfiguration> existentFacetConfigurations) {
			if (!existentFacetConfigurations.isEmpty()) {
			  return existentFacetConfigurations.iterator().next();
			}
			return createDefaultConfiguration();
		}
	}

	private static class LapgFacetPresentation extends DetectedFacetPresentation {
		@Override
		public String getAutodetectionPopupText(@NotNull Module module, @NotNull FacetType facetType, @NotNull String facetName, @NotNull VirtualFile[] files) {
			return LapgBundle.message("facet.detected");
		}
	}
}
