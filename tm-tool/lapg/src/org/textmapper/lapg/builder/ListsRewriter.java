/**
 * Copyright 2002-2016 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.util.RhsUtil;

import java.util.*;

/**
 * evgeny, 2/1/13
 */
public class ListsRewriter {

	private enum ListKind {
		NONE,
		LEFT_RECURSIVE,
		RIGHT_RECURSIVE,
	}

	private LiSymbol symbol;

	public ListsRewriter(LiSymbol symbol) {
		this.symbol = symbol;
	}

	/**
	 * Replaces the definition of a list nonterminal with RhsList.
	 * (see rewrite.tm)
	 */
	public boolean rewrite() {
		if (!(symbol instanceof Nonterminal)) return false;

		Nonterminal nonterm = (Nonterminal) symbol;
		if (!(nonterm.getDefinition() instanceof RhsChoice)) return false;

		// TODO check if any parts of the current definition are mapped

		RhsChoice def = (RhsChoice) nonterm.getDefinition();
		ListKind kind = getListKind(def);
		if (kind != ListKind.NONE) {

			// TODO respect RhsMapping ?

			RhsList list = createList(def, kind == ListKind.RIGHT_RECURSIVE);
			// debug("Rewriting\n" + def.toString() + "\n" + "with\n" + list.toString() + "\n");
			((LiRhsRoot) def).rewrite(list);
			return true;
		}
		return false;
	}

	private static ListKind getListKind(RhsChoice def) {
		boolean immLeft = false;
		boolean immRight = false;

		final Nonterminal list = def.getLeft();
		for (RhsPart rule : def.getParts()) {
			if (!(rule instanceof RhsSequence)) {
				if (!RhsUtil.containsRef(rule, list)) {
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
				} else if (RhsUtil.containsRef(s, list)) {
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
		List<LiRhsPart> initialElements = new ArrayList<>();
		List<LiRhsSequence> listRules = new ArrayList<>();
		LiRhsPart emptyRule = null;

		final Nonterminal list = def.getLeft();
		for (RhsPart rule : def.getParts()) {
			if (rule instanceof RhsSequence) {
				RhsPart[] ruleSeq = ((RhsSequence) rule).getParts();
				RhsPart p = ruleSeq.length > 0
						? ruleSeq[rightRecursive ? ruleSeq.length - 1 : 0] : null;
				if (p instanceof RhsSymbol && ((RhsSymbol) p).getTarget() == list) {
					listRules.add((LiRhsSequence) rule);
					continue;
				}
				if (ruleSeq.length == 0) {
					emptyRule = (LiRhsPart) rule;
					continue;
				}
			}
			initialElements.add((LiRhsPart) RhsUtil.unwrap(rule));
		}

		assert !listRules.isEmpty();
		List<LiRhsSymbol> separator = getCommonSeparator(listRules, rightRecursive);

		int skipParts = 1 + (separator != null ? separator.size() : 0);
		List<LiRhsPart> elements = extractListElements(listRules, skipParts, rightRecursive);

		final LiRhsSequence element = asSequence(merge(elements, MergeStrategy.CHOICE));
		LiRhsSequence customInitialElement =
				asSequence(merge(initialElements, MergeStrategy.CHOICE));
		LiRhsSequence rewritten = null;
		if (customInitialElement != null && element.structurallyEquals(customInitialElement)) {
			rewritten = customInitialElement;
			customInitialElement = null;
		}

		// Note: lists with separator should have at least one element
		if (emptyRule != null && (customInitialElement != null || separator != null)) {
			rewritten = null;
			initialElements.add(emptyRule);
			customInitialElement = asSequence(merge(initialElements, MergeStrategy.CHOICE));
			if (element.structurallyEquals(customInitialElement)) {
				rewritten = customInitialElement;
				customInitialElement = null;
			}
			emptyRule = null;
		}

		if (rewritten != null) {
			registerRewrite(rewritten, element);
		}

		return new LiRhsList(element, merge(separator, MergeStrategy.SEQUENCE), emptyRule == null,
				customInitialElement, rightRecursive, true, def);
	}

	private static void registerRewrite(RhsSymbol from, RhsSymbol to) {
		assert from.getUserData(RhsSymbol.UD_REWRITTEN) == null;
		from.putUserData(RhsSymbol.UD_REWRITTEN, to);
	}

	private static void registerRewrite(RhsPart from, RhsPart to) {
		final RhsSymbol[] fromArr = RhsUtil.getRhsSymbols(from);
		final RhsSymbol[] toArr = RhsUtil.getRhsSymbols(to);
		assert fromArr.length == toArr.length;

		for (int i = 0; i < fromArr.length; i++) {
			assert ((LiRhsPart) fromArr[i]).structurallyEquals((LiRhsPart) toArr[i]);
			registerRewrite(fromArr[i], toArr[i]);
		}
	}

	private static List<LiRhsPart> extractListElements(List<LiRhsSequence> listRules,
													   int skipParts, boolean rightRecursive) {
		List<LiRhsPart> result = new ArrayList<>();

		for (LiRhsSequence part : listRules) {
			RhsPart[] ruleSeq = part.getParts();
			if (ruleSeq.length == skipParts + 1) {
				result.add((LiRhsPart) ruleSeq[rightRecursive ? 0 : ruleSeq.length - 1]);
			} else {
				LiRhsPart[] newArr = new LiRhsPart[ruleSeq.length - skipParts];
				//noinspection SuspiciousSystemArraycopy
				System.arraycopy(ruleSeq, rightRecursive ? 0 : skipParts,
						newArr, 0, newArr.length);
				result.add(new LiRhsSequence(null, newArr, true, part));
			}
		}

		return result;
	}

	private static List<LiRhsSymbol> getCommonSeparator(Collection<LiRhsSequence> listRules,
														boolean rightRecursive) {
		if (listRules.isEmpty()) return null;

		int maxLen = Integer.MAX_VALUE;
		for (RhsSequence seq : listRules) {
			maxLen = Math.min(seq.getParts().length - 2, maxLen);
		}

		List<LiRhsSymbol> result = new ArrayList<>(maxLen);

		for (int i = 0; i < maxLen; i++) {
			Terminal sep = null;
			RhsSymbol first = null;
			LinkedList<RhsSymbol> rewriteCandidates = new LinkedList<>();

			for (LiRhsSequence rule : listRules) {
				RhsPart[] parts = rule.getParts();
				RhsPart part = parts[rightRecursive ? parts.length - 2 - i : 1 + i];
				Terminal curr = asConstantTerminal(part);
				if (curr == null || sep != null && sep != curr) {
					sep = null;
					break;
				}
				if (first == null) {
					first = (RhsSymbol) part;
					sep = curr;
				} else {
					rewriteCandidates.add((RhsSymbol) part);
				}
			}

			if (sep == null) {
				break;
			}

			result.add((LiRhsSymbol) first);
			for (RhsSymbol c : rewriteCandidates) {
				registerRewrite(c, first);
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
			return first;
		}
		final LiRhsPart[] partsArray = parts.toArray(new LiRhsPart[parts.size()]);
		return strategy == MergeStrategy.CHOICE
				? new LiRhsChoice(partsArray, true, first)
				: new LiRhsSequence(null, partsArray, true, first);
	}

	private static LiRhsSequence asSequence(LiRhsPart part) {
		if (part instanceof RhsSequence) {
			return (LiRhsSequence) part;
		}
		return new LiRhsSequence(null, new LiRhsPart[]{part}, true, part);
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
