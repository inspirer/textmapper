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
package org.textmapper.idea.actions;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.TextmapperBundle;

public class TMTemplatesFactory implements FileTemplateGroupDescriptorFactory {

	public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
		final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(
				TextmapperBundle.message("template.file.title"), TMIcons.TM_ICON);
		group.addTemplate(new FileTemplateDescriptor("GrammarForJava.tm", TMIcons.TM_ICON));
		group.addTemplate(new FileTemplateDescriptor("GrammarForJS.tm", TMIcons.TM_ICON));
		return group;
	}
}
