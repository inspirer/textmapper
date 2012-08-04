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
package org.textmapper.lapg.idea.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.lapg.idea.lang.syntax.lexer.LapgTokenTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 1/26/11
 */
public class LapgFoldingBuilder implements FoldingBuilder {

	@NotNull
	public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
		List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        collectDescriptors(node, document, descriptors);
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
	}

	private void collectDescriptors(ASTNode node, Document document, List<FoldingDescriptor> descriptors) {
        final IElementType type = node.getElementType();

        if (isFoldable(type)) {
            TextRange adjustedFoldingRange = node.getTextRange();
            descriptors.add(new FoldingDescriptor(node, adjustedFoldingRange));
        }

		ASTNode child = node.getFirstChildNode();
		while(child != null) {
			collectDescriptors(child, document, descriptors);
			child = child.getTreeNext();
		}
	}

	public String getPlaceholderText(@NotNull ASTNode node) {
		final IElementType type = node.getElementType();
		if (!isFoldable(type)) {
			return null;
		}

		if(type == LapgTokenTypes.TEMPLATES) {
			return "%% {...}";
		}

		return "{...}";
	}

	public boolean isCollapsedByDefault(@NotNull ASTNode node) {
		return false;
	}

	private static boolean isFoldable(IElementType type) {
		return type == LapgTokenTypes.TOKEN_ACTION
				|| type == LapgTokenTypes.TEMPLATES;
	}
}
