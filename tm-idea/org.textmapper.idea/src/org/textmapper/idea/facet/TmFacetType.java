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
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.lang.syntax.TMFileType;

import javax.swing.*;

public class TmFacetType extends FacetType<TmFacet, TmFacetConfiguration> {

	public static final FacetTypeId<TmFacet> ID = new FacetTypeId<TmFacet>("textmapper");

	public static TmFacetType getInstance() {
		return findInstance(TmFacetType.class);
	}

	public TmFacetType() {
		super(ID, TmFacetConstants.TM_FACET_ID, TmFacetConstants.TM_FACET_NAME);
	}

	@Override
	public TmFacetConfiguration createDefaultConfiguration() {
		return new TmFacetConfiguration();
	}

	@Override
	public TmFacet createFacet(@NotNull Module module, String name, @NotNull TmFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
		return new TmFacet(this, module, name, configuration, underlyingFacet);
	}

	@Override
	public boolean isSuitableModuleType(ModuleType moduleType) {
		return true;
	}

	@Override
	public Icon getIcon() {
		return TMIcons.TM_ICON;
	}

	public static class TmFacetDetector extends FacetBasedFrameworkDetector<TmFacet, TmFacetConfiguration> {

		private TmFacetDetector() {
			super("textmapper");
		}

		@Override
		public FacetType<TmFacet, TmFacetConfiguration> getFacetType() {
			return TmFacetType.getInstance();
		}

		@NotNull
		@Override
		public FileType getFileType() {
			return TMFileType.INSTANCE;
		}

		@NotNull
		@Override
		public ElementPattern<FileContent> createSuitableFilePattern() {
			return FileContentPattern.fileContent();
		}
	}
}
