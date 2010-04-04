/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package net.sf.lapg.lalr;

import net.sf.lapg.INotifier;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.Prio;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;
import net.sf.lapg.api.SymbolRef;

abstract class ContextFree {

	private static int getSituations(Rule[] rules) {
		int counter = 0;
		for (Rule rule : rules) {
			counter += rule.getRight().length + 1;
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
		for( int i = 0; i < prios.length; i++ ) {
			result[curr_prio++] = - prios[i].getPrio();
			Symbol[] list = prios[i].getSymbols();
			for (Symbol element : list) {
				result[curr_prio++] = element.getIndex();
			}
		}

		assert curr_prio == counter;
		return result;
	}

	private static int[] toArray(Symbol[] input) {
		int[] result = new int[input.length];
		for(int i = 0; i < input.length; i++) {
			result[i] = input[i].getIndex();
		}
		return result;
	}

	protected ContextFree(Grammar g, INotifier err, int debuglev) {
		this.err = err;
		this.debuglev = debuglev;

		this.sym = g.getSymbols();

		this.nsyms = sym.length;
		this.rules = g.getRules().length;
		this.nterms = g.getTerminals();
		this.inputs = toArray(g.getInput());
		this.eoi = g.getEoi().getIndex();
		this.errorn = g.getError() == null ? -1 : g.getError().getIndex();

		this.situations = getSituations(g.getRules());

		this.priorul = getPriorityRules(g.getPriorities());

		this.rleft = new int[rules];
		this.rprio = new int[rules];
		this.rindex = new int[rules];
		this.rright = new int[situations];
		this.sym_empty = new boolean[nsyms];

		int curr_rindex = 0;
		Rule[] wrules = g.getRules();
		for (int i = 0; i < wrules.length; i++) {
			Rule r = wrules[i];
			this.rleft[i] = r.getLeft().getIndex();
			this.rprio[i] = r.getPriority();
			this.rindex[i] = curr_rindex;
			SymbolRef[] wright = r.getRight();
			for (SymbolRef element : wright) {
				this.rright[curr_rindex++] = element.getTarget().getIndex();
			}
			this.rright[curr_rindex++] = -1-i;
			if( wright.length == 0 ) {
				sym_empty[rleft[i]] = true;
			}
		}

		assert situations == curr_rindex;
	}

	// log

	protected final int debuglev;
	protected final INotifier err;

	// grammar information

	protected final int nsyms, nterms, eoi, errorn;
	protected final int[] inputs;
	protected final int rules, situations;
	protected final Symbol[] sym;
	protected final int[] priorul;
	protected final int[] rleft, rindex, rright, rprio;
	protected final boolean[] sym_empty;

	// info

	protected void print_situation(int situation) {
		int rulenum, i;

		for (i = situation; rright[i] >= 0; i++) {
			;
		}
		rulenum = -rright[i] - 1;

		// left part of the rule
		err.debug("  " + sym[rleft[rulenum]].getName() + " ::=");

		for (i = rindex[rulenum]; rright[i] >= 0; i++) {
			if (i == situation) {
				err.debug(" _");
			}
			err.debug(" " + sym[rright[i]].getName());
		}
		if (i == situation) {
			err.debug(" _");
		}
		err.debug("\n");
	}

	protected void warn_rule(int rule) {
		int rr = rindex[rule];

		err.warn("  " + sym[rleft[rule]].getName() + " ::=");

		for (; rright[rr] >= 0; rr++) {
			err.warn(" " + sym[rright[rr]].getName());
		}

		err.warn("\n");
	}
}
