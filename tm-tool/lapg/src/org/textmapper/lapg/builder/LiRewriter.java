package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.rule.RhsChoice;

/**
 * evgeny, 2/1/13
 */
public class LiRewriter {

	private enum ListKind {
		NONE,
		LEFT_RECURSIVE,
		RIGHT_RECURSIVE,
	}

	private LiSymbol[] symbols;

	public LiRewriter(LiSymbol[] symbols) {
		this.symbols = symbols;
	}

	/**
	 * Finds list non-terminals and replaces their definition with RhsList.
	 */
	public void rewriteLists() {
		for (LiSymbol symbol : symbols) {
			if (!(symbol instanceof Nonterminal)) continue;

			Nonterminal nonterm = (Nonterminal) symbol;
			if (!(nonterm.getDefinition() instanceof RhsChoice)) continue;

			RhsChoice def = (RhsChoice) nonterm.getDefinition();
//			ListKind kind = getListKind(nonterm);
//			if (kind != ListKind.NONE) {
//				LiRhsList list = new LiRhsList()
//			}
		}
	}

//	private static ListKind getListKind(Nonterminal nonterm) {
//		boolean immLeft = false;
//		boolean immRight = false;
//
//		for (Rule rule : nonterm.getRules()) {
//			final RhsSymbol[] right = rule.getRight();
//			if (right.length == 0) continue;
//
//			for (RhsSymbol s : right) {
//				if (s.getTarget() != nonterm) continue;
//				if (s == right[0]) {
//					immLeft = true;
//				} else if (s == right[right.length - 1]) {
//					immRight = true;
//				} else {
//					return ListKind.NONE;
//				}
//
//			}
//		}
//
//		if (immLeft && immRight) {
//			return ListKind.NONE;
//		}
//
//		return immLeft ? ListKind.LEFT_RECURSIVE
//				: immRight ? ListKind.RIGHT_RECURSIVE
//				: ListKind.NONE;
//	}
}
