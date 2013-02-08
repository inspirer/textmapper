package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.common.RuleUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
			ListKind kind = getListKind(def);
			if (kind != ListKind.NONE) {
				RhsList list = createList(def, kind == ListKind.RIGHT_RECURSIVE);
				// debug("Rewriting\n" + def.toString() + "\nwith\n" + list.toString() + "\n");
				((LiRhsPart) def).rewrite(list);
			}
		}
	}

	private static ListKind getListKind(RhsChoice def) {
		boolean immLeft = false;
		boolean immRight = false;

		final Nonterminal list = def.getLeft();
		for (RhsPart rule : def.getParts()) {
			if (!(rule instanceof RhsSequence)) {
				if (!RuleUtil.containsRef(rule, list)) {
					continue;
				}

				// too complex to analyze, i.e. not a list
				return ListKind.NONE;
			}

			final RhsPart[] ruleSeq = ((RhsSequence) rule).getParts();
			for (RhsPart s : ruleSeq) {
				if (s instanceof RhsSymbol) {
					if (((RhsSymbol) s).getTarget() != list) continue;
					if (s == ruleSeq[0]) {
						immLeft = true;
					} else if (s == ruleSeq[ruleSeq.length - 1]) {
						immRight = true;
					} else {
						return ListKind.NONE;
					}
				} else if (RuleUtil.containsRef(s, list)) {
					return ListKind.NONE;
				}
			}
		}

		if (immLeft && immRight) {
			return ListKind.NONE;
		}

		return immLeft ? ListKind.LEFT_RECURSIVE
				: immRight ? ListKind.RIGHT_RECURSIVE
				: ListKind.NONE;
	}

	private static RhsList createList(RhsChoice def, boolean rightRecursive) {
		List<LiRhsPart> initialElements = new ArrayList<LiRhsPart>();
		List<LiRhsSequence> listRules = new ArrayList<LiRhsSequence>();
		LiRhsPart emptyRule = null;

		final Nonterminal list = def.getLeft();
		for (RhsPart rule : def.getParts()) {
			if (rule instanceof RhsSequence) {
				RhsPart[] ruleSeq = ((RhsSequence) rule).getParts();
				RhsPart p = ruleSeq.length > 0 ? ruleSeq[rightRecursive ? ruleSeq.length - 1 : 0] : null;
				if (p instanceof RhsSymbol && ((RhsSymbol) p).getTarget() == list) {
					listRules.add((LiRhsSequence) rule);
					continue;
				}
				if (ruleSeq.length == 0) {
					emptyRule = (LiRhsPart) rule;
					continue;
				}
				if (ruleSeq.length == 1) {
					// unwrap sequence
					initialElements.add((LiRhsPart) ruleSeq[0]);
					continue;
				}
			}
			initialElements.add((LiRhsPart) rule);
		}

		assert !listRules.isEmpty();
		List<LiRhsSymbol> separator = getCommonSeparator(listRules, rightRecursive);

		List<LiRhsPart> elements = new ArrayList<LiRhsPart>();
		int skipParts = 1 + (separator != null ? separator.size() : 0);
		for (LiRhsSequence part : listRules) {
			RhsPart[] ruleSeq = part.getParts();
			if (ruleSeq.length == skipParts + 1) {
				elements.add((LiRhsPart) ruleSeq[rightRecursive ? 0 : ruleSeq.length - 1]);
			} else {
				LiRhsPart[] newArr = new LiRhsPart[ruleSeq.length - skipParts];
				System.arraycopy(ruleSeq, rightRecursive ? 0 : skipParts, newArr, 0, newArr.length);
				elements.add(new LiRhsSequence(LiRhsPart.copyOfArray(newArr), part));
			}
		}

		LiRhsPart element = merge(elements, MergeStrategy.CHOICE);
		LiRhsPart customInitialElement = merge(initialElements, MergeStrategy.CHOICE);
		if (customInitialElement != null && element.structuralEquals(customInitialElement)) {
			customInitialElement = null;
		}

		if (emptyRule != null && (customInitialElement != null || separator != null)) {
			initialElements.add(emptyRule);
			customInitialElement = merge(initialElements, MergeStrategy.CHOICE);
			if (element.structuralEquals(customInitialElement)) {
				customInitialElement = null;
			}
			emptyRule = null;
		}

		return new LiRhsList(element, merge(separator, MergeStrategy.SEQUENCE), emptyRule == null, customInitialElement, rightRecursive, def);
	}

	private static List<LiRhsSymbol> getCommonSeparator(Collection<LiRhsSequence> listRules, boolean rightRecursive) {
		if (listRules.isEmpty()) return null;

		int maxLen = Integer.MAX_VALUE;
		for (RhsSequence seq : listRules) {
			maxLen = Math.min(seq.getParts().length - 2, maxLen);
		}

		List<LiRhsSymbol> result = new ArrayList<LiRhsSymbol>(maxLen);

		for (int i = 0; i < maxLen; i++) {
			Terminal sep = null;
			RhsPart first = null;
			for (LiRhsSequence rule : listRules) {
				RhsPart[] parts = rule.getParts();
				RhsPart part = parts[rightRecursive ? parts.length - 2 - i : 1 + i];
				Terminal curr = asConstantTerminal(part);
				if (first == null) {
					first = part;
					sep = curr;
				}
				if (curr == null || sep != curr) {
					sep = null;
					break;
				}
			}
			if (sep != null) {
				result.add((LiRhsSymbol) first);
			} else {
				break;
			}
		}

		if (rightRecursive) {
			Collections.reverse(result);
		}

		return result.isEmpty() ? null : result;
	}

	private enum MergeStrategy {
		CHOICE,
		SEQUENCE
	}

	private static LiRhsPart merge(List<? extends LiRhsPart> parts, MergeStrategy strategy) {
		if (parts == null || parts.isEmpty()) {
			return null;
		}

		final LiRhsPart first = parts.get(0);
		if (parts.size() == 1) {
			return first.copy();
		}
		final LiRhsPart[] partsCopy = LiRhsPart.copyOfArray(parts.toArray(new LiRhsPart[parts.size()]));
		return strategy == MergeStrategy.CHOICE
				? new LiRhsChoice(partsCopy, first)
				: new LiRhsSequence(partsCopy, first);
	}

	private static Terminal asConstantTerminal(RhsPart part) {
		if (part instanceof RhsSymbol && ((RhsSymbol) part).getTarget() instanceof Terminal) {
			Terminal term = (Terminal) ((RhsSymbol) part).getTarget();
			if (term.isConstant()) {
				return term;
			}
		}
		return null;
	}
}
