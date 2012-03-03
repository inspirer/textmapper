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
package org.textway.lapg.idea.file;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.LapgIcons;
import org.textway.lapg.idea.lang.templates.LapgTemplatesLanguage;

import javax.swing.*;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public class LapgTemplatesFileType extends LanguageFileType {

	public static final LapgTemplatesFileType LAPG_TEMPLATES_FILE_TYPE = new LapgTemplatesFileType();
	public static final Language LAPG_TEMPLATES_LANGUAGE = LAPG_TEMPLATES_FILE_TYPE.getLanguage();

	public static final String DEFAULT_EXTENSION = "ltp";

	private LapgTemplatesFileType() {
		super(new LapgTemplatesLanguage());
	}

	@NotNull
	public String getName() {
		return LapgTemplatesLanguage.ID;
	}

	@NotNull
	public String getDescription() {
		return "Lapg Templates Bundle";
	}

	@NotNull
	public String getDefaultExtension() {
		return DEFAULT_EXTENSION;
	}

	public Icon getIcon() {
		return LapgIcons.LAPG_TEMPLATES_ICON;
	}
}
