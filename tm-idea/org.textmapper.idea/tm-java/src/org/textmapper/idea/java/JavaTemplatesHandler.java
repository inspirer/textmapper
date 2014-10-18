/**
 * Copyright (c) 2010-2014 Evgeny Gryaznov
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
package org.textmapper.idea.java;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.actions.TemplatesHandler;

import java.util.Properties;

public class JavaTemplatesHandler extends TemplatesHandler {
	@Override
	@Nullable
	public void customizeProperties(Properties properties, PsiDirectory directory, final String name,
									String fileName, String templateName) {
		JavaTemplateUtil.setPackageNameAttribute(properties, directory);
	}

}
