/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.lalr;

import org.textway.lapg.api.Symbol;

/**
 * Representation of generated parser tables.
 */
public class ParserTables {

	public Symbol[] sym;
	public int rules, nsyms, nterms, nstates, errorn;
	public int[] rleft, rright, rindex, rprio;
	public short[] sym_goto, sym_from, sym_to, action_table;
	public int[] action_index;
	public int[] final_states;
	public int nactions;

	public int[] getRuleLength() {
		int[] result = new int[rules];
		for (int i = 0; i < rules; i++) {
			int e = 0;
			for (; rright[rindex[i] + e] >= 0; e++) {
			}
			result[i] = e;
		}
		return result;
	}
}