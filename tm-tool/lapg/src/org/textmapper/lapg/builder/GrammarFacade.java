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

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;

public class GrammarFacade {

	public static GrammarBuilder createBuilder() {
		return new LiGrammarBuilder();
	}

	public static GrammarMapper createMapper(Grammar grammar) {
		return new LiGrammarMapper(grammar);
	}

	public static AstBuilder createAstBuilder() {
		return new LiAstBuilder();
	}
}
