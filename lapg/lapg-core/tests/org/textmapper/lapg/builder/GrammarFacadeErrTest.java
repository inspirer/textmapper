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
package org.textmapper.lapg.builder;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.Lexem;
import org.textmapper.lapg.api.Prio;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.builder.GrammarBuilder;

import java.util.Collections;

/**
 * evgeny, 6/29/12
 */
public class GrammarFacadeErrTest {

	@Test(expected = NullPointerException.class)
	public void testErrorNullName() {
		GrammarFacade.createBuilder().addSymbol(Symbol.KIND_TERM, null, "string", null);
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorDuplicateName() {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorDuplicateSoftName() {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol symbol = builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		builder.addSoftSymbol("sym", symbol, null);
	}

	@Test(expected = NullPointerException.class)
	public void testErrorNullSymbol() throws Exception {
		GrammarFacade.createBuilder().addLexem(
				Lexem.KIND_NONE, null, LapgCore.parse("id", "a"), 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorCrossBuilderSymbols() throws Exception {
		Symbol symbolFromAnotherBuilder = GrammarFacade.createBuilder().addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		GrammarFacade.createBuilder().addLexem(
				Lexem.KIND_NONE, symbolFromAnotherBuilder, LapgCore.parse("id", "a"), 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorRuleForTerm() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol term = builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		builder.rule(null, term, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorTermAsInput() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol term = builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		builder.addInput(term, true, null);
	}

	@Test(expected = NullPointerException.class)
	public void testErrorNoRegexp() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol term = builder.addSymbol(Symbol.KIND_TERM, "sym", "string", null);
		builder.addLexem(Lexem.KIND_NONE, term, null, 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorNonTermAsLexem() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol nonterm = builder.addSymbol(Symbol.KIND_NONTERM, "input", null, null);
		builder.addLexem(Lexem.KIND_NONE, nonterm, LapgCore.parse("id", "a"), 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorSoftVsNonSoft() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol classterm = builder.addSymbol(Symbol.KIND_TERM, "id", "string", null);
		Symbol softterm = builder.addSoftSymbol("keyword", classterm, null);
		builder.addLexem(Lexem.KIND_NONE, softterm, LapgCore.parse("id", "a"), 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorSoftVsNonSoft2() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol term = builder.addSymbol(Lexem.KIND_NONE, "s", "s", null);
		builder.addLexem(Lexem.KIND_SOFT, term, LapgCore.parse("id", "a"), 1, 0, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorSoftSymbolUsingWrongMethod() throws Exception {
		GrammarFacade.createBuilder().addSymbol(Symbol.KIND_SOFTTERM, "s", "s", null);
	}

	@Test(expected = NullPointerException.class)
	public void testErrorSoftNullClass() throws Exception {
		GrammarFacade.createBuilder().addSoftSymbol("name", null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorBadPriority() throws Exception {
		GrammarFacade.createBuilder().addPrio(100, Collections.<Symbol>emptyList(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorUsingNontermAsPrio() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		Symbol nonterm = builder.addSymbol(Symbol.KIND_NONTERM, "input", null, null);
		builder.addPrio(Prio.LEFT, Collections.singleton(nonterm), null);
	}

	@Test(expected = NullPointerException.class)
	public void testErrorPatternNoName() throws Exception {
		GrammarFacade.createBuilder().addPattern(null, LapgCore.parse("a", "a"), null);
	}

	@Test(expected = NullPointerException.class)
	public void testErrorPatternNoRegex() throws Exception {
		GrammarFacade.createBuilder().addPattern("a", null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorDuplicatePattern() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addPattern("a", LapgCore.parse("a", "a"), null);
		builder.addPattern("a", LapgCore.parse("b", "b"), null);
	}
}
