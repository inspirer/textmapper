/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * evgeny, 6/29/12
 */
public class GrammarFacadeTest {

	@Test
	public void testSimpleGrammar() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();

		// id: /[a-z][a-z0-9]+/
		// input : id ;

		LexerState initial = builder.addState(LapgCore.name("initial"), null);

		Terminal id = builder.addTerminal(LapgCore.name("id"), null, null);
		builder.addLexerRule(LexerRule.KIND_NONE, id, LapgCore.parse("id", "[a-z][a-z0-9]+"), Collections.singleton(initial), 0, 0, null, null);


		Nonterminal input = builder.addNonterminal(LapgCore.name("input"), null);
		builder.addInput(input, true, null);
		builder.addRule(input, builder.asSequence(builder.symbol(id, null, null)));

		Grammar grammar = builder.create();

		Symbol[] symbols = grammar.getSymbols();
		assertEquals(3, symbols.length);
		assertTrue(grammar.getEoi() == symbols[0]);
		assertEquals("id", symbols[1].getNameText());
		assertTrue(symbols[1].isTerm());
		assertEquals("input", symbols[2].getNameText());
		assertFalse(symbols[2].isTerm());
		for (int i = 0; i < symbols.length; i++) {
			assertEquals(i, symbols[i].getIndex());
		}

		Rule[] rules = grammar.getRules();
		assertEquals(1, rules.length);
		assertEquals(id.getIndex(), grammar.getRules()[0].getPrecedence());
		assertTrue(symbols[2] == rules[0].getLeft());
		assertEquals(1, rules[0].getRight().length);
		assertTrue(symbols[1] == rules[0].getRight()[0].getTarget());

		// lexer
		LexerRule[] lRules = grammar.getLexerRules();
		assertEquals(1, lRules.length);
		assertFalse(lRules[0].isExcluded());
		assertTrue(symbols[1] == lRules[0].getSymbol());
		assertNull(lRules[0].getClassRule());
		assertEquals("none", lRules[0].getKindAsText());
		assertEquals("[a-z][a-z0-9]+", lRules[0].getRegexp().toString());
		assertLexerStates(Collections.singleton(initial), lRules[0].getStates());

		// input
		InputRef[] inputRefs = grammar.getInput();
		assertEquals(1, inputRefs.length);
		assertNull(((DerivedSourceElement) inputRefs[0]).getOrigin());
		assertEquals(symbols[2], inputRefs[0].getTarget());
		assertEquals(true, inputRefs[0].hasEoi());

		// grammar
		assertNull(grammar.getError());
		assertEquals(2, grammar.getTerminals());
		assertEquals(3, grammar.getGrammarSymbols());

		// empty lists
		assertEquals(0, grammar.getPatterns().length);
		assertEquals(0, grammar.getPriorities().length);
	}

	@Test
	public void testLexerOnlyGrammar() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();

		// id: /[a-z]+/  (class)
		// kw:  /keyword/
		// spc: /[\t ]+/

		LexerState initial = builder.addState(LapgCore.name("initial"), null);

		Terminal id = builder.addTerminal(LapgCore.name("id"), raw("string"), null);
		LexerRule idLexerRule = builder.addLexerRule(LexerRule.KIND_CLASS, id, LapgCore.parse("id", "[a-z]+"), Collections.singleton(initial), 0, 0, null, null);

		Terminal kw = builder.addTerminal(LapgCore.name("kw"), null, null);
		builder.makeSoft(kw, id);
		builder.addLexerRule(LexerRule.KIND_SOFT, kw, LapgCore.parse("kw", "keyword"), Collections.singleton(initial), 0, 0, idLexerRule, null);
		Terminal spc = builder.addTerminal(LapgCore.name("spc"), null, null);
		builder.addLexerRule(LexerRule.KIND_SPACE, spc, LapgCore.parse("spc", "[\t ]+"), Collections.singleton(initial), 0, 0, null, null);
		Grammar grammar = builder.create();

		Symbol[] symbols = grammar.getSymbols();
		assertEquals(4, symbols.length);
		assertTrue(grammar.getEoi() == symbols[0]);

		// id
		assertEquals("id", symbols[1].getNameText());
		assertTrue(symbols[1].isTerm());
		assertTrue(symbols[1] instanceof Terminal);
		assertFalse(((Terminal)symbols[1]).isSoft());
		assertNull(((Terminal)symbols[1]).getSoftClass());

		// kw
		assertEquals("kw", symbols[2].getNameText());
		assertTrue(symbols[2].isTerm());
		assertTrue(symbols[2] instanceof Terminal);
		assertTrue(((Terminal)symbols[2]).isSoft());
		assertTrue(symbols[1] == ((Terminal)symbols[2]).getSoftClass());

		// spc
		assertEquals("spc", symbols[3].getNameText());
		assertTrue(symbols[3].isTerm());
		for (int i = 0; i < symbols.length; i++) {
			assertEquals(i, symbols[i].getIndex());
			assertNull(((DerivedSourceElement) symbols[i]).getOrigin());
		}

		LexerRule[] lRules = grammar.getLexerRules();
		for (int i = 0; i < lRules.length; i++) {
			assertEquals(i, lRules[i].getIndex());
			assertNull(((DerivedSourceElement) lRules[i]).getOrigin());
			assertEquals(0, lRules[i].getPriority());
		}
		assertEquals(3, lRules.length);
		// id
		assertTrue(symbols[1] == lRules[0].getSymbol());
		assertNull(lRules[0].getClassRule());
		assertEquals("[a-z]+", lRules[0].getRegexp().toString());
		assertLexerStates(Collections.singleton(initial), lRules[0].getStates());
		assertEquals(LexerRule.KIND_CLASS, lRules[0].getKind());
		assertEquals("class", lRules[0].getKindAsText());
		// kw
		assertTrue(symbols[2] == lRules[1].getSymbol());
		assertTrue(lRules[1].getClassRule() == lRules[0]);
		assertEquals("keyword", lRules[1].getRegexp().toString());
		assertTrue(lRules[1].getRegexp().isConstant());
		assertEquals("keyword", lRules[1].getRegexp().getConstantValue());
		assertLexerStates(Collections.singleton(initial), lRules[1].getStates());
		assertEquals(LexerRule.KIND_SOFT, lRules[1].getKind());
		assertEquals("soft", lRules[1].getKindAsText());
		assertTrue(lRules[1].isExcluded());
		// spc
		assertTrue(symbols[3] == lRules[2].getSymbol());
		assertEquals(LexerRule.KIND_SPACE, lRules[2].getKind());
		assertEquals("space", lRules[2].getKindAsText());
		assertLexerStates(Collections.singleton(initial), lRules[2].getStates());

		// empty lists
		assertNull(grammar.getRules());
		assertEquals(0, grammar.getPatterns().length);
		assertNull(grammar.getPriorities());
		assertNull(grammar.getInput());
		assertNull(grammar.getError());

		//
		assertEquals(4, grammar.getTerminals());
		assertEquals(4, grammar.getGrammarSymbols());
	}

	@Test
	public void testEoi() throws Exception {
		Symbol eoi = GrammarFacade.createBuilder().getEoi();
		assertEquals(0, eoi.getIndex());
		assertEquals(Symbol.EOI, eoi.getName());
		assertNull(eoi.getType());
		assertTrue(eoi.isTerm());
	}

	@Test
	public void testPrio() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();

		// id: /[a-z][a-z0-9]+/
		// input : id ;

		LexerState initial = builder.addState(LapgCore.name("initial"), null);

		Terminal id = builder.addTerminal(LapgCore.name("id"), null, null);
		builder.addLexerRule(LexerRule.KIND_NONE, id, LapgCore.parse("id", "[a-z][a-z0-9]+"), Collections.singleton(initial), 0, 0, null, null);

		Nonterminal input = builder.addNonterminal(LapgCore.name("input"), null);
		builder.addInput(input, true, null);

		RhsSymbol symbol = builder.symbol(id, null, null);
		RhsSequence rhs = builder.addPrecedence(symbol, id);
		builder.addRule(input, rhs);

		assertNotNull(builder.addPrio(Prio.RIGHT, Collections.singleton(id), null));

		Grammar grammar = builder.create();

		assertEquals(1, grammar.getRules().length);
		assertEquals(id.getIndex(), grammar.getRules()[0].getPrecedence());
		assertEquals(1, grammar.getPriorities().length);
		assertEquals(Prio.RIGHT, grammar.getPriorities()[0].getPrio());
		assertEquals(1, grammar.getPriorities()[0].getSymbols().length);
		assertTrue(id == grammar.getPriorities()[0].getSymbols()[0]);
		assertNull(((DerivedSourceElement) grammar.getPriorities()[0]).getOrigin());
	}

	@Test
	public void testNamedPatterns() throws Exception {
		GrammarBuilder builder = GrammarFacade.createBuilder();

		// pattern = /[a-z]+/
		// id:  /{pattern}/

		LexerState initial = builder.addState(LapgCore.name("initial"), null);

		builder.addPattern(LapgCore.name("pattern"), LapgCore.parse("pattern", "[a-z]+"), null);
		Terminal id = builder.addTerminal(LapgCore.name("id"), raw("string"), null);
		builder.addLexerRule(LexerRule.KIND_NONE, id, LapgCore.parse("id", "{pattern}"), Collections.singleton(initial), 0, 0, null, null);
		Grammar grammar = builder.create();

		NamedPattern[] patterns = grammar.getPatterns();
		assertEquals(1, patterns.length);
		assertEquals("pattern", patterns[0].getNameText());
		assertEquals("[a-z]+", patterns[0].getRegexp().toString());
		assertNull(((DerivedSourceElement) patterns[0]).getOrigin());
	}

	private void assertLexerStates(Iterable<LexerState> expected, Iterable<LexerState> actual) {
		Iterator<LexerState> expectedIterator = expected.iterator();
		Iterator<LexerState> actualIterator = actual.iterator();
		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			assertEquals(expectedIterator.next(), actualIterator.next());
		}
		assertEquals(expectedIterator.hasNext(), actualIterator.hasNext());
	}

	private static AstType raw(String type) {
		return GrammarFacade.createAstBuilder().rawType(type, null);
	}
}
