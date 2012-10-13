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
package org.textmapper.idea.lang.syntax.formatter;

import com.intellij.formatting.*;
import com.intellij.formatting.Indent.Type;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.lang.syntax.LapgFileType;
import org.textmapper.idea.lang.syntax.lexer.LapgTokenTypes;
import org.textmapper.idea.lang.syntax.parser.LapgElementTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * evgeny, 8/14/12
 */
public class TMFormattingBlock extends AbstractBlock {

	protected Indent indent;
	protected CodeStyleSettings settings;
	protected SpacingBuilder spacingBuilder;
	protected List<Block> children;

	public TMFormattingBlock(@NotNull ASTNode node,
							 @Nullable Alignment alignment,
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

	protected boolean isAfter(final int newChildIndex, final IElementType[] elementTypes) {
		final Block previousBlock = newChildIndex == 0 ? null : getSubBlocks().get(newChildIndex - 1);
		if (!(previousBlock instanceof AbstractBlock)) {
			return false;
		}
		final IElementType previousElementType = ((AbstractBlock) previousBlock).getNode().getElementType();
		for (IElementType elementType : elementTypes) {
			if (previousElementType == elementType) {
				return true;
			}
		}
		return false;
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

		ASTNode prev = null;
		for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
			IElementType childType = child.getElementType();

			if (child.getTextRange().getLength() == 0) continue;

			if (childType == TokenType.WHITE_SPACE) {
				continue;
			}

			blocks.add(buildChild(child, prev, null));
			prev = child;
		}
		return Collections.unmodifiableList(blocks);
	}

	@NotNull
	protected Block buildChild(@NotNull ASTNode child, ASTNode prev, Alignment childAlignment) {
		IElementType elementType = child.getElementType();
		if (elementType == LapgElementTypes.NONTERM) {
			return new NonTermBlock(child, null, settings, spacingBuilder);
		}
		return new TMFormattingBlock(child, childAlignment, createChildIndent(child, prev), null, settings, spacingBuilder);
	}

	@NotNull
	@Override
	public ChildAttributes getChildAttributes(int newChildIndex) {
		return new ChildAttributes(Indent.getNoneIndent(), null);
	}

	@Nullable
	protected Indent createChildIndent(@NotNull ASTNode child, ASTNode prev) {
		return Indent.getNoneIndent();
	}

	public static class NonTermBlock extends TMFormattingBlock {

		private Alignment lastCodeBlock = Alignment.createAlignment(true);

		public NonTermBlock(@NotNull ASTNode node, Wrap wrap, CodeStyleSettings settings, SpacingBuilder spacingBuilder) {
			super(node, null, Indent.getNoneIndent(), wrap, settings, spacingBuilder);
		}

		@NotNull
		@Override
		protected Block buildChild(@NotNull ASTNode child, ASTNode prev, Alignment childAlignment) {
			if (child.getElementType() == LapgElementTypes.RULE) {
				return new RuleBlock(child, null, settings, spacingBuilder, lastCodeBlock, prev == null || prev.getElementType() == LapgTokenTypes.OP_CCEQ);
			}
			return super.buildChild(child, prev, childAlignment);
		}

		@NotNull
		@Override
		public ChildAttributes getChildAttributes(int newChildIndex) {
			if (isAfter(newChildIndex, new IElementType[]{LapgElementTypes.TYPE, LapgElementTypes.IDENTIFIER})) {
				return new ChildAttributes(Indent.getNoneIndent(), null);
			} else if (isAfter(newChildIndex, new IElementType[]{LapgTokenTypes.OP_CCEQ})) {
				return new ChildAttributes(Indent.getIndent(Type.SPACES, settings.getIndentSize(LapgFileType.LAPG_FILE_TYPE) + 2, false, true), null);
			} else {
				if (getSubBlocks().size() == newChildIndex) {
					return new ChildAttributes(Indent.getNoneIndent(), null);
				} else {
					return new ChildAttributes(Indent.getNormalIndent(), null);
				}
			}
		}

		@Override
		protected Indent createChildIndent(@NotNull ASTNode child, ASTNode prev) {
			IElementType type = child.getElementType();
			if (type == LapgTokenTypes.OP_OR || type == LapgElementTypes.RULE) {
				return Indent.getNormalIndent();
			}

			return Indent.getNoneIndent();
		}
	}

	public static class RuleBlock extends TMFormattingBlock {

		private final Alignment lastCodeBlock;
		private final boolean first;

		public RuleBlock(@NotNull ASTNode node, Wrap wrap, CodeStyleSettings settings,
						 SpacingBuilder spacingBuilder,
						 Alignment lastCodeBlock, boolean isFirst) {
			super(node, null, Indent.getIndent(Indent.Type.NORMAL, false, true), wrap, settings, spacingBuilder);
			this.lastCodeBlock = lastCodeBlock;
			this.first = isFirst;
		}

		@NotNull
		@Override
		protected Block buildChild(@NotNull ASTNode child, ASTNode prev, Alignment childAlignment) {
			if (child.getElementType() == LapgElementTypes.ACTION) {
				ASTNode treeNext = child.getTreeNext();
				while (treeNext != null && treeNext.getElementType() == TokenType.WHITE_SPACE) {
					treeNext = treeNext.getTreeNext();
				}
				if (treeNext == null || treeNext.getElementType() == LapgElementTypes.RULEATTRS) {
					childAlignment = lastCodeBlock;
				}
			}
			return super.buildChild(child, prev, childAlignment);
		}

		@NotNull
		@Override
		public ChildAttributes getChildAttributes(int newChildIndex) {
			if (getSubBlocks().size() == newChildIndex) {
				return new ChildAttributes(Indent.getNoneIndent(), null);
			} else {
				return new ChildAttributes(Indent.getContinuationIndent(), null);
			}
		}

		@Override
		protected Indent createChildIndent(@NotNull ASTNode child, ASTNode prev) {
			IElementType type = child.getElementType();
			if (prev == null) {
				if (first) {
					return Indent.getSpaceIndent(2);
				} else {
					return Indent.getNoneIndent();
				}
			}
			if (type == LapgElementTypes.REF_RULEPART || type == LapgElementTypes.ACTION
					|| type == LapgElementTypes.UNORDERED_RULEPART || type == LapgElementTypes.RULEATTRS) {
				return Indent.getContinuationIndent();
			}

			return Indent.getNoneIndent();
		}
	}

}
