/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.psi.TmTemplates;
import org.textmapper.idea.lang.templates.LtplFileType;
import org.textmapper.idea.lang.templates.parser.LtplParser;

/**
 * evgeny, 3/4/12
 */
public class TMTemplatesElementType extends ILazyParseableElementType {

	private int symbol;
	private boolean contentOnly;

	public TMTemplatesElementType(int symbol, boolean contentOnly, @NotNull String debugName) {
		super(debugName, LtplFileType.LTPL_LANGUAGE);
		this.symbol = symbol;
		this.contentOnly = contentOnly;
	}

	public int getSymbol() {
		return symbol;
	}

	@Override
	public ASTNode createNode(CharSequence text) {
		return new TmTemplates(this, text);
	}

	@Override
	protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
		final Project project = psi.getProject();
		Language languageForParser = getLanguageForParser(psi);
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, chameleon.getChars());
		final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(project);
		if (contentOnly) {
			return ((LtplParser) parser).parseBody(this, builder).getFirstChildNode();
		} else {
			return parser.parse(this, builder).getFirstChildNode();
		}
	}

	@Override
	public String toString() {
		return "[tm]" + super.toString();
	}
}
