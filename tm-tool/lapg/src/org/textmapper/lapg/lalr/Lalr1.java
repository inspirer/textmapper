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

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.ProcessingStatus;

import java.text.MessageFormat;
import java.util.Arrays;

class Lalr1 extends LR0 {

	static class Short {
		int value;
		Short next;
	}

	Lalr1(Grammar g, ProcessingStatus status) {
		super(g, status);
	}

	// LALR

	int[] larule /* index in LA -> rule */, laindex /* state -> index in LA */;
	int[] LA /* (state,rule to reduce in state) -> setof(term) */;
	int[] term_goto /* sym -> index [nsyms + 1] */, term_from, term_to /* [ntgotos + ngotos] for each shift: state->state */;

	private int maxrpart /* max len of rule's right part */, ngotos, ntgotos;
	private Short[] lookback /* [number of available non-LR0 reductions] */;
	private int[] edge;
	private int[][] graph;
	private int[] follow /* ngotos ->setof(term) */;

	protected void buildLalr() {
		LA = null;
		laindex = larule = null;

		initializeLA();
		init_goto();

		for (int i = nterms; i <= nsyms; i++) {
			term_goto[i] -= ntgotos;
		}

		edge = new int[ngotos + 1];
		graph = new int[ngotos][];

		init_follow();
		build_follow();
		show_follow();

		buildLA();
		show_lookaheads();

		edge = null;
		graph = null;

		freeLA();

		// TODO
		for (int i = nterms; i <= nsyms; i++) {
			term_goto[i] += ntgotos;
		}
	}


	// fills: laindex, larule, maxrpart; creates: lookback, LA
	private void initializeLA() {
		int i, e, k;
		State t;

		// calculate maxrpart
		maxrpart = 2;
		for (e = i = 0; i < items; i++) {
			if (rright[i] < 0) {
				if (e > maxrpart) {
					maxrpart = e;
				}
				e = 0;
			} else {
				e++;
			}
		}

		// get larule size
		for (e = 0, t = first; t != null; t = t.next) {
			if (!t.LR0) {
				e += t.nreduce;
			}
		}

		if (e == 0) {
			// TODO: LR(0) grammar - exit?
			e = 1;
		}

		// allocate
		laindex = new int[nstates + 1];
		laindex[nstates] = e;
		larule = new int[e];
		lookback = new Short[e];
		LA = new int[e * termset];

		Arrays.fill(lookback, null);
		Arrays.fill(LA, 0);

		// fills: larule, laindex
		for (i = 0, t = first; t != null; t = t.next) {
			laindex[t.number] = i;
			if (!t.LR0) {
				for (k = 0; k < t.nreduce; k++) {
					larule[i++] = t.reduce[k];
				}
			}
		}
	}


	// fills: term_goto, term_from, term_to
	private void init_goto() {
		int i, e, symnum;

		term_goto = new int[nsyms + 1];
		int[] symshiftCounter = new int[nsyms];
		Arrays.fill(symshiftCounter, 0);

		ngotos = 0;
		for (State t = first; t != null; t = t.next) {
			for (i = t.nshifts - 1; i >= 0; i--) {
				symnum = state[t.shifts[i]].symbol;
				symshiftCounter[symnum]++;
				ngotos++;

				// handle soft terms
				if(symnum < nterms && classterm[symnum] == -1 && !t.softConflicts) {
					for (int soft = softterms[symnum]; soft != -1; soft = softterms[soft]) {
						symshiftCounter[soft]++;
						ngotos++;
					}
				}
			}
		}

		for (e = i = 0; i < nsyms; i++) {
			term_goto[i] = e;
			e += symshiftCounter[i];
			symshiftCounter[i] = term_goto[i];
		}
		term_goto[nsyms] = ngotos;
		term_from = new int[ngotos];
		term_to = new int[ngotos];

		for (State t = first; t != null; t = t.next) {
			for (i = t.nshifts - 1; i >= 0; i--) {
				int newstate = t.shifts[i];
				symnum = state[newstate].symbol;
				e = symshiftCounter[symnum]++;
				term_from[e] = t.number;
				term_to[e] = newstate;

				// handle soft terms
				if(symnum < nterms && classterm[symnum] == -1 && !t.softConflicts) {
					for (int soft = softterms[symnum]; soft != -1; soft = softterms[soft]) {
						e = symshiftCounter[soft]++;
						term_from[e] = t.number;
						term_to[e] = newstate;
					}
				}
			}
		}

		ntgotos = term_goto[nterms];
		ngotos -= ntgotos;
	}


	// returns the number of goto, which shifts from state by non-term symbol
	private int select_goto(int state, int symbol) {
		int min = term_goto[symbol], max = term_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = term_from[ntgotos + e];
			if (i == state) {
				return e;
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}

		assert false;
		return 0;
	}


	// add goto number to rule lookback list
	private void add_lookback(int state, int rule, int gotono) {
		int i = laindex[state], max = laindex[state + 1];

		for (; i < max; i++) {
			if (larule[i] == rule) {
				Short s = new Short();
				s.value = gotono;
				s.next = lookback[i];
				lookback[i] = s;
				return;
			}
		}

		assert false;
	}


	// builds in-rule follow set, processes empty symbols
	private void init_follow() {
		int settrav = 0, nedges = 0;
		int[][] empties = graph;

		follow = new int[ngotos * termset];
		Arrays.fill(follow, 0);

		for (int i = 0; i < ngotos; i++, settrav += termset) {
			int st = term_to[ntgotos + i];
			int[] shifts = state[st].shifts;
			int nshifts = state[st].nshifts, shifts_ind = 0;

			// handle goto for no-eoi input => add all terms into follow
			if (term_from[ntgotos + i] < inputs.length) {
				int src = term_from[ntgotos + i];
				if (noEoiInput[src] && final_states[src] == st) {
					for (int k = 0; k < termset - 1; k++) {
						follow[settrav + k] = ~0;
					}
					for (int k = (termset - 1) * BITS; k < nterms; k++) {
						follow[settrav + termset - 1] |= (1 << (k % BITS));
					}
				}
			}

			for (; shifts_ind < nshifts && state[shifts[shifts_ind]].symbol < nterms; shifts_ind++) {
				int sym = state[shifts[shifts_ind]].symbol;
				follow[settrav + sym / BITS] |= (1 << (sym % BITS));

				// add soft terms
				if(classterm[sym] == -1) {
					for (int soft = softterms[sym]; soft != -1; soft = softterms[soft]) {
						follow[settrav + soft / BITS] |= (1 << (soft % BITS));
					}
				}
			}

			for (; shifts_ind < nshifts; shifts_ind++) {
				int k = state[shifts[shifts_ind]].symbol;
				if (sym_empty[k]) {
					edge[nedges++] = select_goto(st, k);
				}
			}

			if (nedges != 0) {
				empties[i] = new int[nedges + 1];
				System.arraycopy(edge, 0, empties[i], 0, nedges);
				empties[i][nedges] = -1;
				nedges = 0;
			} else {
				empties[i] = null;
			}
		}

		graph_closure(empties);
		Arrays.fill(empties, null);
	}

	private int state_by_symbol(int sourceState, int symbol) {
		assert state[sourceState].nshifts == state[sourceState].shifts.length;

		for (int target : state[sourceState].shifts) {
			if (state[target].symbol == symbol) {
				return target;
			}
		}

		// data consistency problem, shouldn't happen
		throw new RuntimeException("state N" + sourceState + " is broken, cannot shift " + sym[symbol].getName());
	}

	// builds 1) lookback 2) cross-rule follow graph & updates follow set
	private void build_follow() {
		int i, length, currstate, nedges = 0, rpart;
		int[] states = new int[maxrpart + 1];

		for (i = 0; i < ngotos; i++) {
			int fstate = term_from[ntgotos + i];
			int symbol = state[term_to[ntgotos + i]].symbol;

			for (int rule : derives[symbol - nterms]) {
				currstate = states[0] = fstate;
				length = 1;

				// iterate through rule's states
				for (rpart = rindex[rule]; rright[rpart] >= 0; rpart++) {
					currstate = state_by_symbol(currstate, rright[rpart]);
					states[length++] = currstate;
				}

				if (!state[currstate].LR0) {
					// a) lookback: rule's lookahead symbols include follow set for the current goto (i)
					add_lookback(currstate, rule, i);
				}

				for (length--; ; ) {
					rpart--;
					if (rpart >= 0 && rright[rpart] >= nterms) {
						currstate = states[--length];

						// b) inner rule's goto inherits outer follow set
						edge[nedges++] = select_goto(currstate, rright[rpart]);
						if (sym_empty[rright[rpart]]) {
							continue;
						}
					}
					break;
				}
			}

			if (nedges != 0) {
				graph[i] = new int[nedges + 1];
				System.arraycopy(edge, 0, graph[i], 0, nedges);
				graph[i][nedges] = -1;
				nedges = 0;
			} else {
				graph[i] = null;
			}
		}

		show_graph();
		graph = transpose_graph(graph, ngotos);
		show_graph();

		graph_closure(graph);
	}


	// fills: LA
	private void buildLA() {
		final int n = laindex[nstates];
		Short s;
		int p, i, from;

		for (p = 0, i = 0; i < n; i++, p += termset) {

			// add all associated gotos
			for (s = lookback[i]; s != null; s = s.next) {
				from = termset * s.value;
				for (int q = 0; q < termset; q++) {
					LA[p + q] |= follow[from + q];
				}
			}

			// free lookback
			lookback[i] = null;
		}
	}


	// frees temporary data
	private void freeLA() {
		lookback = null;
		follow = null;
	}

	private static final String spaces = "    ";

	private static String format(int l, boolean left) {
		String s = Integer.toString(l);
		if (s.length() >= 4) {
			return s;
		}
		if (left) {
			return spaces.substring(s.length()) + s;
		}
		return s + spaces.substring(s.length());
	}

	// debug
	private void show_follow() {
		if (!status.isDebugMode()) {
			return;
		}

		int i, e;

		status.debug("\nFollow:\n");
		for (i = 0; i < ngotos; i++) {

			status.debug(format(term_from[ntgotos + i], false) + " -> " + format(term_to[ntgotos + i], true) + "\t");
			for (e = 0; e < nterms; e++) {
				if (((follow[i * termset + (e) / BITS] & (1 << ((e) % BITS))) != 0)) {
					status.debug(" " + sym[e].getName());
				}
			}

			status.debug("\n");
		}
	}


	// debug
	private void show_graph() {
		if (!status.isDebugMode()) {
			return;
		}

		status.debug("\nGraph:\n");

		for (int i = 0; i < ngotos; i++) {
			int[] p = graph[i];
			if (p != null) {
				status.debug(MessageFormat.format(" {0,number,####}: ", i));
				for (int e = 0; p[e] >= 0; e++) {
					status.debug(" " + p[e]);
				}
				status.debug("\n");
			}
		}

	}


	// debug
	private void show_lookaheads() {
		if (!status.isAnalysisMode()) {
			return;
		}

		status.debug("\nLookaheads:\n");

		for (int i = 0; i < nstates; i++) {
			if (laindex[i] < laindex[i + 1]) {
				int max = laindex[i + 1];
				status.debug(i + ":\n");

				for (int e = laindex[i]; e < max; e++) {
					int k = rindex[larule[e]];
					while (rright[k] >= 0) {
						k++;
					}
					status.debug("  " + debugText(k) + "\n");
					int set = termset * e;

					status.debug("  >>>");
					for (k = 0; k < nterms; k++) {
						if (((LA[set + (k) / BITS] & (1 << ((k) % BITS))) != 0)) {
							status.debug(" " + sym[k].getName());
						}
					}
					status.debug("\n");
				}
			}
		}

	}

	// reverts all edges in graph
	private static int[][] transpose_graph(int[][] graph, final int n) {
		int[] nedges = new int[n];
		int[] p;
		int i;

		// calculate new row sizes
		Arrays.fill(nedges, 0);
		for (i = 0; i < n; i++) {
			p = graph[i];
			if (p != null) {
				for (int e = 0; p[e] >= 0; e++) {
					nedges[p[e]]++;
				}
			}
		}

		// allocate new graph
		int[][] newgraph = new int[n][];
		for (i = 0; i < n; i++) {
			newgraph[i] = (nedges[i] != 0) ? new int[nedges[i] + 1] : null;
		}

		// fill new graph
		Arrays.fill(nedges, 0);
		for (i = 0; i < n; i++) {
			p = graph[i];
			if (p != null) {
				for (int e = 0; p[e] >= 0; e++) {
					newgraph[p[e]][nedges[p[e]]++] = i;
				}
			}
		}

		// insert -1 (end-of-row markers)
		for (i = 0; i < n; i++) {
			if (newgraph[i] != null) {
				newgraph[i][nedges[i]] = -1;
			}
		}

		return newgraph;
	}

	// graph closure // //////////////////////////////////////////////////////////////////////////////

	private int infinity, top;
	private int[][] relation;
	private int[] gc_index, gc_vertices;

	// process one vertex
	private void do_vertex(int i) {
		int height, k;

		gc_vertices[++top] = i;
		height = top;
		gc_index[i] = top;

		if (relation[i] != null) {
			int[] row = relation[i];
			for (int e = 0; row[e] >= 0; e++) {
				if (gc_index[row[e]] == 0) {
					do_vertex(row[e]);
				}

				if (gc_index[i] > gc_index[row[e]]) {
					gc_index[i] = gc_index[row[e]];
				}

				int from = termset * row[e], to = termset * i;
				for (k = 0; k < termset; k++) {
					follow[to++] |= follow[from++];
				}
			}
		}

		if (gc_index[i] == height) {
			for (; ; ) {
				int e = gc_vertices[top--];
				gc_index[e] = infinity;
				if (i == e) {
					break;
				}
				int from = termset * i, to = termset * e;
				for (k = 0; k < termset; k++) {
					follow[to++] |= follow[from++];
				}
			}
		}
	}

	// modifies: follow (in according to digraph)
	private void graph_closure(int[][] relation) {
		int i;

		this.relation = relation;
		gc_index = new int[ngotos];
		gc_vertices = new int[ngotos + 1];
		infinity = ngotos + 2;
		top = 0;

		for (i = 0; i < ngotos; i++) {
			gc_index[i] = 0;
		}

		for (i = 0; i < ngotos; i++) {
			if (gc_index[i] == 0 && relation[i] != null) {
				do_vertex(i);
			}
		}

		gc_index = null;
		gc_vertices = null;
		this.relation = null;
	}
}
