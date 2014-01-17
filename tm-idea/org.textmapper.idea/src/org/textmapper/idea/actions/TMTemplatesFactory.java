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
package org.textmapper.idea.actions;

import com.intellij.ide.fileTemplates.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.textmapper.idea.TMIcons;
import org.textmapper.idea.TextmapperBundle;
import org.textmapper.idea.lang.syntax.TMFileType;
import org.textmapper.templates.eval.DefaultStaticMethods;

import java.util.Properties;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class TMTemplatesFactory implements FileTemplateGroupDescriptorFactory {

	@NonNls
	public static final String GRAMMAR_FILE = "LapgGrammar.s";
	@NonNls
	static final String NAME_TEMPLATE_PROPERTY = "NAME";
	@NonNls
	static final String NAME_CAP_TEMPLATE_PROPERTY = "NAME_CAP";

	public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
		final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(TextmapperBundle.message("template.file.title"),
				TMIcons.TM_ICON);
		group.addTemplate(new FileTemplateDescriptor(GRAMMAR_FILE, TMIcons.TM_ICON));
		return group;
	}

	public static PsiFile createFromTemplate(final PsiDirectory directory, final String name, String fileName, String templateName,
											 @NonNls String... parameters) throws IncorrectOperationException {
		final FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate(templateName);
		Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());
		JavaTemplateUtil.setPackageNameAttribute(properties, directory);
		properties.setProperty(NAME_TEMPLATE_PROPERTY, name);
		properties.setProperty("DOLLAR", "$");
		properties.setProperty(NAME_CAP_TEMPLATE_PROPERTY, new DefaultStaticMethods().toFirstUpper(name));
		for (int i = 0; i < parameters.length; i += 2) {
			properties.setProperty(parameters[i], parameters[i + 1]);
		}
		String text;
		try {
			text = template.getText(properties);
		} catch (Exception e) {
			throw new RuntimeException("Unable to load template for " + FileTemplateManager.getInstance().internalTemplateToSubject(templateName), e);
		}

		final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
		final PsiFile file = factory.createFileFromText(fileName, TMFileType.INSTANCE, text);
		return (PsiFile) directory.add(file);
	}

}
