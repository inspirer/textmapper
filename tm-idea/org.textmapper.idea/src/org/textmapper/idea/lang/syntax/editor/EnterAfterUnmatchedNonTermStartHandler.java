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

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.TMTokenTypes;

/**
 * Gryaznov Evgeny, 9/21/12
 */
public class EnterAfterUnmatchedNonTermStartHandler extends EnterHandlerDelegateAdapter {
	private static final Logger LOG = Logger.getInstance("#org.textmapper.idea.lang.syntax.editor.EnterAfterUnmatchedNonTermStartHandler");

	public Result preprocessEnter(@NotNull final PsiFile file, @NotNull final Editor editor, @NotNull final Ref<Integer> caretOffsetRef, @NotNull final Ref<Integer> caretAdvance,
								  @NotNull final DataContext dataContext, final EditorActionHandler originalHandler) {
		Document document = editor.getDocument();
		CharSequence text = document.getCharsSequence();
		int caretOffset = caretOffsetRef.get();
		if (caretOffset < 3 || !("::=".equals(text.subSequence(caretOffset - 3, caretOffset).toString()))) {
			return Result.Continue;
		}

		boolean isIncomplete = isIncompleteNonTerm(editor, caretOffset);
		if (!CodeInsightSettings.getInstance().INSERT_BRACE_ON_ENTER || !(isIncomplete)) {
			return Result.Continue;
		}

		int offset = CharArrayUtil.shiftForward(text, caretOffset, " \t");
		if (offset < document.getTextLength()) {
			offset = CharArrayUtil.shiftForwardUntil(text, caretOffset, "\n");
		}
		offset = Math.min(offset, document.getTextLength());

		document.insertString(offset, "\n;");
		PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);

		return Result.DefaultForceIndent;
	}

	private static boolean isIncompleteNonTerm(Editor editor, int offset) {
		EditorHighlighter highlighter = ((EditorEx) editor).getHighlighter();
		HighlighterIterator iterator = highlighter.createIterator(offset);

		for (; !iterator.atEnd(); iterator.advance()) {
			IElementType tokenType = iterator.getTokenType();
			if (tokenType == TMTokenTypes.OP_CCEQ || tokenType == TMTokenTypes.OP_PERCENT) {
				return true;
			}
			if (tokenType == TMTokenTypes.OP_SEMICOLON) {
				return false;
			}
		}

		return true;
	}
}
