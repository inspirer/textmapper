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
package org.textway.lapg;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.LexerData;
import org.textway.lapg.api.ParserData;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.builder.GrammarBuilder;
import org.textway.lapg.api.regex.RegexContext;
import org.textway.lapg.api.regex.RegexMatcher;
import org.textway.lapg.api.regex.RegexParseException;
import org.textway.lapg.api.regex.RegexPart;
import org.textway.lapg.builder.GrammarFacade;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.regex.RegexFacade;

import java.util.Map;

/**
 * Gryaznov Evgeny, 6/13/12
 */
public class LapgCore {

	public static GrammarBuilder createBuilder() {
		return GrammarFacade.createBuilder();
	}

	public static ParserData generateParser(Grammar g, ProcessingStatus status) {
		return Builder.compile(g, status);
	}

	public static LexerData generateLexer(Grammar g, ProcessingStatus status) {
		return LexicalBuilder.compile(g.getLexems(), g.getPatterns(), status);
	}

	public static RegexMatcher createMatcher(RegexPart regex, RegexContext context) {
		return RegexFacade.createMatcher(regex, context);
	}

	public static RegexContext createContext(final Map<String, RegexPart> map) {
		return RegexFacade.createContext(map);
	}

	public static RegexPart parse(String alias, String regex) throws RegexParseException {
		return RegexFacade.parse(alias, regex);
	}
}
