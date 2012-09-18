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
package org.textmapper.lapg.idea.lang.syntax;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.textmapper.lapg.idea.lang.syntax.psi.TmLexem;
import org.textmapper.lapg.idea.lang.syntax.psi.TmStateReference;
import org.textmapper.lapg.idea.lang.syntax.psi.TmSymbolReference;

/**
 * Gryaznov Evgeny, 1/30/11
 */
public class LapgAnnotator implements Annotator {
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if (element instanceof TmSymbolReference) {
			TmSymbolReference ref = (TmSymbolReference) element;
			PsiElement target = ref.resolve();
			if (target instanceof TmLexem) {
				Annotation infoAnnotation = holder.createInfoAnnotation(ref, null);
				infoAnnotation.setTextAttributes(LapgSyntaxHighlighter.LEXEM_REFERENCE);
			} else if (target == null) {
				Annotation infoAnnotation = holder.createErrorAnnotation(ref, "cannot resolve `" + ref.getReferenceText() + "'");
				infoAnnotation.setTextAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
			}
		}
		if (element instanceof TmStateReference) {
			TmStateReference ref = (TmStateReference) element;
			if (ref.multiResolve(false).length == 0) {
				Annotation infoAnnotation = holder.createErrorAnnotation(ref, "cannot resolve state `" + ref.getReferenceText() + "'");
				infoAnnotation.setTextAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
			}
		}
	}
}
