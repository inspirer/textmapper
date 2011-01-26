/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lexer.LapgTokenTypes;

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
