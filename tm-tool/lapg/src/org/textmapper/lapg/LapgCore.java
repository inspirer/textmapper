/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.lapg;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;
import org.textmapper.lapg.api.regex.RegexContext;
import org.textmapper.lapg.api.regex.RegexMatcher;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.builder.GrammarFacade;
import org.textmapper.lapg.lalr.Builder;
import org.textmapper.lapg.lex.LexerGenerator;
import org.textmapper.lapg.regex.RegexFacade;

import java.util.Map;

/**
 * Gryaznov Evgeny, 6/13/12
 */
public class LapgCore {

	public static GrammarBuilder createBuilder() {
		return GrammarFacade.createBuilder();
	}

	public static GrammarMapper createMapper(Grammar g) {
		return GrammarFacade.createMapper(g);
	}

	public static AstBuilder createAstBuilder() {
		return GrammarFacade.createAstBuilder();
	}

	public static Name name(String... aliases) {
		return GrammarFacade.name(aliases);
	}

	public static ParserData generateParser(Grammar g, ProcessingStatus status) {
		return Builder.compile(g, status);
	}

	public static LexerData generateLexer(Grammar g, ProcessingStatus status) {
		return LexerGenerator.generate(g.getLexerStates(), g.getLexerRules(), g.getPatterns(), status);
	}

	/**
	 * @throws RegexParseException if regex contains expand parts that cannot be resolved in the given context.
	 */
	public static RegexMatcher createMatcher(RegexPart regex, RegexContext context) throws RegexParseException {
		return RegexFacade.createMatcher(regex, context);
	}

	public static RegexContext createContext(final Map<String, RegexPart> map) {
		return RegexFacade.createContext(map);
	}

	public static RegexPart parse(String alias, String regex) throws RegexParseException {
		return RegexFacade.parse(alias, regex);
	}
}
