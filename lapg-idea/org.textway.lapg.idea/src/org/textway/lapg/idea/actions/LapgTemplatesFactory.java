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
package org.textway.lapg.idea.actions;

import com.intellij.ide.fileTemplates.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.textway.lapg.idea.LapgBundle;
import org.textway.lapg.idea.LapgIcons;

import java.util.Properties;

/**
 * Gryaznov Evgeny, 3/13/11
 */
public class LapgTemplatesFactory implements FileTemplateGroupDescriptorFactory {

	@NonNls
	public static final String GRAMMAR_FILE = "LapgGrammar.s";
	@NonNls
	static final String NAME_TEMPLATE_PROPERTY = "NAME";

	public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
		final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(LapgBundle.message("template.file.title"),
				LapgIcons.LAPG_ICON);
		group.addTemplate(new FileTemplateDescriptor(GRAMMAR_FILE, LapgIcons.LAPG_ICON));
		return group;
	}

	public static PsiFile createFromTemplate(final PsiDirectory directory, final String name, String fileName, String templateName,
											 @NonNls String... parameters) throws IncorrectOperationException {
		final FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate(templateName);
		Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());
		JavaTemplateUtil.setPackageNameAttribute(properties, directory);
		properties.setProperty(NAME_TEMPLATE_PROPERTY, name);
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
		final PsiFile file = factory.createFileFromText(fileName, text);
		return (PsiFile) directory.add(file);
	}

}
