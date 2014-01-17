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
package org.textmapper.idea.lang.syntax;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.textmapper.idea.TMIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TMFileType extends LanguageFileType {

	public static final TMFileType INSTANCE = new TMFileType();
	public static final Language TM_LANGUAGE = INSTANCE.getLanguage();

	public static final String DEFAULT_EXTENSION = "tm";

    private TMFileType() {
        super(new TextmapperLanguage());
    }


    @NotNull
    public String getName() {
        return TextmapperLanguage.ID;
    }

    @NotNull
    public String getDescription() {
        return "Textmapper source";
    }

    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public Icon getIcon() {
        return TMIcons.TM_ICON;
    }
}
