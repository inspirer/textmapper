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
package org.textway.lapg.parser;

import org.textway.lapg.api.Prio;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.parser.ast.IAstNode;

public class LiPrio extends LiEntity implements Prio {

	private final int prio;
	private final LiSymbol[] symbols;

	public LiPrio(int prio, LiSymbol[] symbols, IAstNode node) {
		super(node);
		this.prio = prio;
		this.symbols = symbols;
	}

	public int getPrio() {
		return prio;
	}

	public Symbol[] getSymbols() {
		return symbols;
	}
}
