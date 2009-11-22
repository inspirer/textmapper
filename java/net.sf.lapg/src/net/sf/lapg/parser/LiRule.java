package net.sf.lapg.parser;

import net.sf.lapg.api.Action;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.parser.ast.Node;

public class LiRule implements Rule {
	
	private static final LiSymbol[] EMPTY_RIGHT = new LiSymbol[0];
	
	private int index;
	private final LiSymbol left;
	private final LiSymbol[] right;
	private final Action code;
	private final LiSymbol priority;
	private final Node node;
	
	public LiRule(LiSymbol left, LiSymbol[] right, Action code, LiSymbol priority, Node node) {
		this.left = left;
		this.right = right == null ? EMPTY_RIGHT : right ;
		this.code = code;
		this.priority = priority;
		this.node = node;
	}

	@Override
	public Action getAction() {
		return code;
	}

	@Override
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public Symbol getLeft() {
		return left;
	}

	@Override
	public Symbol[] getRight() {
		return right;
	}
	
	@Override
	public int getPriority() {
		if (priority != null) {
			return priority.getIndex();
		}
		for (int i = right.length - 1; i >= 0; i--) {
			if (right[i].isTerm()) {
				return right[i].getIndex();
			}
		}
		return -1;
	}
	
	public Node getNode() {
		return node;
	}
}
