/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.file;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.textway.lapg.idea.LapgIcons;
import org.textway.lapg.idea.LapgLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LapgFileType extends LanguageFileType {

	public static final LapgFileType LAPG_FILE_TYPE = new LapgFileType();
	public static final Language LAPG_LANGUAGE = LAPG_FILE_TYPE.getLanguage();

	public static final String DEFAULT_EXTENSION = "s";

    private LapgFileType() {
        super(new LapgLanguage());
    }


    @NotNull
    public String getName() {
        return LapgLanguage.ID;
    }

    @NotNull
    public String getDescription() {
        return "Grammar file";
    }

    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public Icon getIcon() {
        return LapgIcons.LAPG_ICON;
    }
}
