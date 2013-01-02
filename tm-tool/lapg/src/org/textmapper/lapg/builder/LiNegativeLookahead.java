/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.lapg.api.NegativeLookahead;
import org.textmapper.lapg.api.Terminal;

/**
 * Gryaznov Evgeny, 8/15/11
 */
class LiNegativeLookahead implements NegativeLookahead {

	private final Terminal[] symbols;

	public LiNegativeLookahead(Terminal[] symbols) {
		this.symbols = symbols;
	}

	@Override
	public Terminal[] getUnwantedSet() {
		return symbols;
	}
}
