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
package org.textmapper.lapg.idea.lang.syntax.formatter;

import com.intellij.formatting.*;
import com.intellij.formatting.Indent.Type;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.lapg.idea.lang.syntax.parser.LapgElementTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * evgeny, 8/14/12
 */
public class TMFormattingBlock extends AbstractBlock {

	private Indent indent;
	private CodeStyleSettings settings;
	private SpacingBuilder spacingBuilder;
	private List<Block> children;

	public TMFormattingBlock(@NotNull ASTNode node,
							 Alignment alignment,
							 Indent indent,
							 Wrap wrap,
							 CodeStyleSettings settings,
							 SpacingBuilder spacingBuilder) {
		super(node, wrap, alignment);
		this.indent = indent;
		this.settings = settings;
		this.spacingBuilder = spacingBuilder;
	}

	@Override
	public Indent getIndent() {
		return indent;
	}

	@Override
	public Spacing getSpacing(Block child1, Block child2) {
		return spacingBuilder.getSpacing(this, child1, child2);
	}

	@Override
	public boolean isLeaf() {
		return myNode.getFirstChildNode() == null;
	}

	@Override
	protected List<Block> buildChildren() {
		if (children == null) {
			children = buildChildrenInternal();
		}
		return new ArrayList<Block>(children);
	}

	private List<Block> buildChildrenInternal() {
		List<Block> blocks = new ArrayList<Block>();

		for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
			IElementType childType = child.getElementType();

			if (child.getTextRange().getLength() == 0) continue;

			if (childType == TokenType.WHITE_SPACE) {
				continue;
			}

			blocks.add(buildChild(child, null));
		}
		return Collections.unmodifiableList(blocks);
	}

	@NotNull
	private Block buildChild(@NotNull ASTNode child, Alignment childAlignment) {
		Wrap wrap = null;
		return new TMFormattingBlock(child, childAlignment, createChildIndent(child), wrap, settings, spacingBuilder);
	}

	@Nullable
	protected static Indent createChildIndent(@NotNull ASTNode child) {
		IElementType type = child.getElementType();

		if (type == LapgElementTypes.RULES) {
			return Indent.getNormalIndent();
		}
		if (type == LapgElementTypes.RULE) {
			return Indent.getIndent(Type.SPACES, 2, false, true);
		}
		if (type == LapgElementTypes.RULEPART) {
			return Indent.getContinuationWithoutFirstIndent();
		}

		return Indent.getNoneIndent();
	}
}
