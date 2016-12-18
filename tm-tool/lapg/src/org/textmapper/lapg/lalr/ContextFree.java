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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.rule.RhsCFPart;
import org.textmapper.lapg.api.rule.RhsPart.Kind;
import org.textmapper.lapg.api.rule.RhsStateMarker;

import java.util.*;

abstract class ContextFree {

	// log

	protected final ProcessingStatus status;

	// grammar information

	final int nsyms, nterms, eoi, errorn;
	final int[] inputs;
	final boolean[] noEoiInput;
	final int rules, items;
	final Symbol[] sym;
	final Rule[] wrules;
	final int[] priorul;
	final int[] rleft, rindex, rright, rprio;
	final boolean[] sym_empty;
	final int[] classterm; /* index of a class term, or 0 if none, or -1 for class term */
	final int[] softterms; /* a -1 terminated list of soft terms for a class */

	final Map<Integer, Set<String>> itemMarkers = new HashMap<>();


	protected ContextFree(Grammar g, ProcessingStatus status) {
		this.status = status;

		this.sym = g.getSymbols();
		this.wrules = g.getRules();

		this.nsyms = g.getGrammarSymbols();
		this.rules = wrules.length;
		this.nterms = g.getTerminals();

		InputRef[] startRefs = g.getInput();
		this.noEoiInput = new boolean[startRefs.length];
		this.inputs = new int[startRefs.length];
		for (int i = 0; i < startRefs.length; i++) {
			this.inputs[i] = startRefs[i].getTarget().getIndex();
			this.noEoiInput[i] = !startRefs[i].hasEoi();
		}

		this.eoi = g.getEoi().getIndex();
		this.errorn = g.getError() == null ? -1 : g.getError().getIndex();

		this.items = computeItems(wrules);

		this.priorul = getPriorityRules(g.getPriorities());

		this.classterm = new int[nterms];
		this.softterms = new int[nterms];
		Arrays.fill(this.softterms, -1);
		Arrays.fill(this.classterm, 0);
		for (int i = 0; i < nterms; i++) {
			Terminal term = (Terminal) this.sym[i];
			if (term.isSoft()) {
				int classindex = term.getSoftClass().getIndex();
				assert classindex < nterms && this.sym[classindex] instanceof Terminal &&
						!((Terminal) this.sym[classindex]).isSoft();
				this.classterm[i] = classindex;
				this.classterm[classindex] = -1;
				this.softterms[i] = this.softterms[classindex];
				this.softterms[classindex] = i;
			}
		}

		this.rleft = new int[rules];
		this.rprio = new int[rules];
		this.rindex = new int[rules];
		this.rright = new int[items];
		this.sym_empty = new boolean[nsyms];

		int curr_rindex = 0;
		for (int i = 0; i < wrules.length; i++) {
			Rule r = wrules[i];
			this.rleft[i] = r.getLeft().getIndex();
			this.rprio[i] = r.getPrecedence();
			this.rindex[i] = curr_rindex;
			for (RhsCFPart element : r.getRight()) {
				switch (element.getKind()) {
					case Symbol:
						this.rright[curr_rindex++] = element.getTarget().getIndex();
						break;
					case StateMarker: {
						Set<String> set = itemMarkers.get(curr_rindex);
						if (set == null) {
							itemMarkers.put(curr_rindex, set = new HashSet<>());
						}
						set.add(((RhsStateMarker) element).getName());
						break;
					}
				}
			}
			if (this.rindex[i] == curr_rindex) {
				sym_empty[rleft[i]] = true;
			}
			this.rright[curr_rindex++] = -1 - i;
		}

		assert items == curr_rindex;
	}

	private static int computeItems(Rule[] rules) {
		int counter = 0;
		for (Rule rule : rules) {
			counter++;
			for (RhsCFPart p : rule.getRight()) {
				if (p.getKind() == Kind.Symbol) counter++;
			}
		}
		return counter;
	}

	private static int[] getPriorityRules(Prio[] prios) {
		int counter = 0;
		for (Prio prio : prios) {
			counter += prio.getSymbols().length + 1;
		}

		int[] result = new int[counter];

		int curr_prio = 0;
		for (Prio prio : prios) {
			result[curr_prio++] = -prio.getPrio();
			Symbol[] list = prio.getSymbols();
			for (Symbol element : list) {
				result[curr_prio++] = element.getIndex();
			}
		}

		assert curr_prio == counter;
		return result;
	}

	private static int[] toSortedIds(Terminal[] list) {
		int[] result = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			result[i] = list[i].getIndex();
		}
		Arrays.sort(result);
		return result;
	}

	protected int ruleIndex(int item) {
		while (rright[item] >= 0) item++;
		return -rright[item] - 1;
	}

	// info

	protected String debugText(int item) {
		StringBuilder sb = new StringBuilder();
		int rule = ruleIndex(item);

		// left part of the rule
		sb.append(sym[rleft[rule]].getNameText()).append(" ::=");

		int i;
		for (i = rindex[rule]; rright[i] >= 0; i++) {
			if (i == item) {
				sb.append(" _");
			}
			sb.append(" ").append(sym[rright[i]].getNameText());
		}
		if (i == item) {
			sb.append(" _");
		}
		return sb.toString();
	}
}
