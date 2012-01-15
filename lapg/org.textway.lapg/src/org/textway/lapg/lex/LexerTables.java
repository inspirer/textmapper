/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.lapg.lex;

/**
 * Representation of generated lexer tables.
 */
public class LexerTables {

	public final int nstates, nchars, nterms;
	public final int[] lnum, char2no, groupset;
	public final int[][] change;

	public LexerTables(int nstates, int nchars, int nterms, int[] lnum, int[] char2no, int[] groupset, int[][] change) {
		this.nstates = nstates;
		this.nchars = nchars;
		this.nterms = nterms;
		this.lnum = lnum;
		this.char2no = char2no;
		this.groupset = groupset;
		this.change = change;
	}
}
