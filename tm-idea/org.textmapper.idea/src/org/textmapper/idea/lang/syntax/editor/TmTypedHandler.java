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
package org.textmapper.idea.lang.syntax.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;
import org.textmapper.idea.lang.syntax.psi.TmNonterm;

/**
 * Gryaznov Evgeny, 9/21/12
 */
public class TmTypedHandler extends TypedHandlerDelegate {
	@Override
	public Result charTyped(char c, Project project, Editor editor, @NotNull PsiFile file) {
		if (!(file instanceof TMPsiFile)) {
			return Result.CONTINUE;
		}
		if (c == '|' || c == ';') {
			if (autoIndentLine(editor, project, file)) {
				return Result.STOP;
			}
		}
		return Result.CONTINUE;
	}

	private static boolean autoIndentLine(Editor editor, Project project, PsiFile file) {
		int offset = editor.getCaretModel().getOffset();
		PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
		PsiElement currElement = file.findElementAt(offset - 1);
		if (currElement == null || !(currElement.getParent() instanceof TmNonterm)) {
			return false;
		}
		Document document = editor.getDocument();
		CharSequence text = document.getCharsSequence();
		char c = text.charAt(offset - 1);
		if (c != '|' && c != ';') {
			return false;
		}
		offset = CharArrayUtil.shiftBackward(text, offset - 2, " \t");
		if (offset <= 0 || text.charAt(offset) == '\n') {
			CodeStyleManager.getInstance(project).adjustLineIndent(file, currElement.getTextOffset());
			return true;
		}
		return false;
	}

}
