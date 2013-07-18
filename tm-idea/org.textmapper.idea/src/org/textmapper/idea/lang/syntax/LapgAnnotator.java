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

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textmapper.idea.lang.syntax.lexer.TmToken;
import org.textmapper.idea.lang.syntax.psi.*;

import java.util.HashSet;
import java.util.Set;

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
		if (element instanceof TmAnnotation) {
			for (PsiElement el = element.getFirstChild(); el instanceof TmToken || el instanceof PsiWhiteSpace; el = el.getNextSibling()) {
				if (el instanceof PsiWhiteSpace) continue;
				IElementType type = ((TmToken) el).getTokenType();
				if (!(type == LapgTokenTypes.OP_AT || type == LapgTokenTypes.ID)) break;
				Annotation infoAnnotation = holder.createInfoAnnotation(el, null);
				infoAnnotation.setTextAttributes(LapgSyntaxHighlighter.ANNOTATION);
			}
		}
		if (element instanceof TmDirective
				|| element instanceof TmRhsSuffix
				|| element instanceof TmNontermType
				|| element instanceof TmHeader
				|| element instanceof TmLexemAttrs) {
			for (TmToken token : PsiTreeUtil.getChildrenOfTypeAsList(element, TmToken.class)) {
				if (isSoft(((LapgElementType) token.getTokenType()).getSymbol())) {
					Annotation infoAnnotation = holder.createInfoAnnotation((ASTNode) token, null);
					infoAnnotation.setTextAttributes(DefaultLanguageHighlighterColors.KEYWORD);
				}
			}
		}
		if (element instanceof TmRhsPrefix) {
			for (PsiElement el = element.getFirstChild(); el != null; el = el.getNextSibling()) {
				if (el instanceof PsiWhiteSpace) continue;
				if (el instanceof TmToken) {
					IElementType type = ((TmToken) el).getTokenType();
					if (type == LapgTokenTypes.OP_LBRACKET || type == LapgTokenTypes.OP_RBRACKET) {
						Annotation infoAnnotation = holder.createInfoAnnotation(el, null);
						infoAnnotation.setTextAttributes(LapgSyntaxHighlighter.RHS_PREFIX);
					}
				} else if (el instanceof TmIdentifier) {
					Annotation infoAnnotation = holder.createInfoAnnotation(el, null);
					infoAnnotation.setTextAttributes(LapgSyntaxHighlighter.RHS_PREFIX);
				}
			}
		}
	}

	private static Set<Integer> softKeywords = new HashSet<Integer>();

	static {
		for (IElementType softKeyword : LapgTokenTypes.softKeywords.getTypes()) {
			softKeywords.add(((LapgElementType) softKeyword).getSymbol());
		}
	}

	private static boolean isSoft(int symbol) {
		return softKeywords.contains(symbol);
	}
}
