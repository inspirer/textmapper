/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

import org.textmapper.lapg.api.ParserData;
import org.textmapper.lapg.api.Symbol;

/**
 * Representation of generated parser tables.
 */
class ParserTables implements ParserData {

	private Symbol[] sym;
	private int rules, nsyms, nterms, nstates;
	private int[] rleft, rlen;
	private short[] sym_goto, sym_from, sym_to, action_table;
	private int[] action_index;
	private int[] final_states;

	ParserTables(Symbol[] sym,
				 int rules, int nsyms, int nterms, int nstates,
				 int[] rleft, int[] rlen,
				 short[] sym_goto, short[] sym_from, short[] sym_to,
				 short[] action_table, int[] action_index, int[] final_states) {
		this.sym = sym;
		this.rules = rules;
		this.nsyms = nsyms;
		this.nterms = nterms;
		this.nstates = nstates;
		this.rleft = rleft;
		this.rlen = rlen;
		this.sym_goto = sym_goto;
		this.sym_from = sym_from;
		this.sym_to = sym_to;
		this.action_table = action_table;
		this.action_index = action_index;
		this.final_states = final_states;
	}

	public int[] getRuleLength() {
		return rlen;
	}

	@Override
	public Symbol[] getSymbols() {
		return sym;
	}

	@Override
	public int getRules() {
		return rules;
	}

	@Override
	public int getNsyms() {
		return nsyms;
	}

	@Override
	public int getNterms() {
		return nterms;
	}

	@Override
	public int getStatesCount() {
		return nstates;
	}

	@Override
	public short[] getSymGoto() {
		return sym_goto;
	}

	@Override
	public short[] getSymFrom() {
		return sym_from;
	}

	@Override
	public short[] getSymTo() {
		return sym_to;
	}

	@Override
	public short[] getLalr() {
		return action_table;
	}

	@Override
	public int[] getAction() {
		return action_index;
	}

	@Override
	public int[] getFinalStates() {
		return final_states;
	}

	@Override
	public int[] getLeft() {
		return rleft;
	}
}