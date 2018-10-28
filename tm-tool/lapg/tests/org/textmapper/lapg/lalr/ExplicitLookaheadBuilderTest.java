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
package org.textmapper.lapg.lalr;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.test.TestStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

public class ExplicitLookaheadBuilderTest {

	private String resolve(Lookahead... lookaheads) {
		if (lookaheads.length < 2) {
			throw new IllegalArgumentException("2+ lookaheads are expected");
		}

		TestStatus status = new TestStatus();
		ExplicitLookaheadBuilder laBuilder = new ExplicitLookaheadBuilder(0, status);
		laBuilder.addResolutionRule(new LinkedHashSet<>(Arrays.asList(lookaheads)), 7);
		LookaheadRule[] rules = laBuilder.extractRules();
		assertEquals(1, rules.length);
		return rules[0].toString();
	}

	private void expectError(String error, Lookahead... lookaheads) {
		if (lookaheads.length < 2) {
			throw new IllegalArgumentException("2+ lookaheads are expected");
		}

		TestStatus status = new TestStatus("", error);
		ExplicitLookaheadBuilder laBuilder = new ExplicitLookaheadBuilder(0, status);
		laBuilder.addResolutionRule(new LinkedHashSet<>(Arrays.asList(lookaheads)), 7);
		LookaheadRule[] rules = laBuilder.extractRules();
		status.assertDone();
		assertEquals(1, rules.length);
		assertEquals("default -> ERROR", rules[0].toString());
	}

	@Test
	public void testSimple() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);


		Lookahead la1 = builder.lookahead(Collections.singletonList(
				builder.lookaheadPredicate(ifA, true)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Collections.singletonList(
				builder.lookaheadPredicate(ifA, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		// Non-negated first.
		assertEquals("ifA -> lookahead_ifA; default -> lookahead_notifA",
				resolve(la2, la1));
		assertEquals("ifA -> lookahead_ifA; default -> lookahead_notifA",
				resolve(la1, la2));
	}

	@Test
	public void testSorting() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);
		InputRef ifC = builder.addInput(builder.addNonterminal(LapgCore.name("ifC"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		Lookahead la3 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifC, true)
		), builder.addNonterminal(LapgCore.name("context3"), null), null);

		// Sorted by predicate nonterminal.
		assertEquals("!ifA -> lookahead_notifA; ifB -> lookahead_ifB; default -> lookahead_notifC",
				resolve(la2, la3, la1));
	}

	@Test
	public void testMulti() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);
		InputRef ifC = builder.addInput(builder.addNonterminal(LapgCore.name("ifC"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, true),
				builder.lookaheadPredicate(ifC, true)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifB, true),
				builder.lookaheadPredicate(ifC, true)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		assertEquals("ifA -> lookahead_ifA_notifB_notifC; "
				+ "default -> lookahead_notifA_notifB_notifC", resolve(la1, la2));
	}

	@Test
	public void testOverlapping() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);
		InputRef ifC = builder.addInput(builder.addNonterminal(LapgCore.name("ifC"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifC, false)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		Lookahead la3 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifB, true),
				builder.lookaheadPredicate(ifC, true)
		), builder.addNonterminal(LapgCore.name("context3"), null), null);

		assertEquals("ifA -> lookahead_ifA_ifC; "
				+ "ifB -> lookahead_notifA_ifB; "
				+ "default -> lookahead_notifB_notifC", resolve(la3, la1, la2));
		assertEquals("ifA -> lookahead_ifA_ifC; "
				+ "ifB -> lookahead_notifA_ifB; "
				+ "default -> lookahead_notifB_notifC", resolve(la2, la1, la3));
	}

	@Test
	public void testNoConflict() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		assertEquals("default -> lookahead_notifA_ifB", resolve(la1, la2));
	}

	@Test
	public void testInconsistentOrder() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifB, true),
				builder.lookaheadPredicate(ifA, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		// Both lookaheads have the same predicate on A.
		expectError("Conflicting lookaheads (inconsistent nonterminal order): "
				+ "!ifA & ifB, !ifB & ifA\n", la1, la2);
	}

	@Test
	public void testPredicatesSharingCommonPrefix() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifB, true)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		assertEquals("ifB -> lookahead_ifA_ifB; default -> lookahead_ifA_notifB",
				resolve(la1, la2));
	}

	@Test
	public void testComplexPredicates() throws Exception {
		GrammarBuilder builder = LapgCore.createBuilder();

		InputRef ifA = builder.addInput(builder.addNonterminal(LapgCore.name("ifA"), null), false, null);
		InputRef ifB = builder.addInput(builder.addNonterminal(LapgCore.name("ifB"), null), false, null);

		Lookahead la1 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context1"), null), null);

		Lookahead la2 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifB, true)
		), builder.addNonterminal(LapgCore.name("context2"), null), null);

		Lookahead la3 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, false),
				builder.lookaheadPredicate(ifB, false)
		), builder.addNonterminal(LapgCore.name("context3"), null), null);

		Lookahead la4 = builder.lookahead(Arrays.asList(
				builder.lookaheadPredicate(ifA, true),
				builder.lookaheadPredicate(ifB, true)
		), builder.addNonterminal(LapgCore.name("context4"), null), null);

		expectError("Conflicting lookaheads: !ifA & ifB, ifA & !ifB, ifA & ifB, !ifA & !ifB\n",
				la1, la2, la3, la4);
	}
}
