/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.TemplateParameter.Forward;
import org.textmapper.lapg.api.TemplateParameter.Type;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.api.rule.RhsPredicate;
import org.textmapper.lapg.api.rule.RhsPredicate.Operation;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PredicatesTest {

	@Test
	public void testEnvironment() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		Symbol t1 = b.addNonterminal("T1", null);
		Symbol t2 = b.addNonterminal("T2", null);

		TemplateParameter s1 = b.addParameter(Type.Symbol, "s1", t1,
				Forward.Always, null);
		TemplateParameter s2 = b.addParameter(Type.Symbol, "s2", null /* defaultValue */,
				Forward.Always, null);
		TemplateEnvironment env = GrammarFacade.createBuilder().getRootEnvironment();

		assertTrue(env == env.extend(s1, t1));
		assertTrue(env == env.extend(s1, null));

		// With default value
		assertEquals(t1, env.getValue(s1));
		env = env.extend(s1, t2);
		assertEquals(t2, env.getValue(s1));
		env = env.extend(s1, null);
		assertEquals(t1, env.getValue(s1));
		env = env.extend(s1, t1);
		assertEquals(t1, env.getValue(s1));

		// Without default value
		assertEquals(null, env.getValue(s2));
		env = env.extend(s2, t2);
		assertEquals(t2, env.getValue(s2));
		env = env.extend(s2, null);
		assertEquals(null, env.getValue(s2));
		env = env.extend(s2, t1);
		assertEquals(t1, env.getValue(s2));
	}

	@Test
	public void testEvaluation() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		TemplateParameter p1 = b.addParameter(Type.Flag, "p1", Boolean.TRUE, Forward.Always, null);
		TemplateParameter p2 = b.addParameter(Type.Flag, "p2", null, Forward.Always, null);

		RhsPredicate pr = and(b, not(b, equals(b, p1, Boolean.TRUE)),
				equals(b, p2, Boolean.FALSE));
		assertEquals("!(p1 == true) && p2 == false", pr.toString());

		TemplateEnvironment env = b.getRootEnvironment();
		assertEquals(false, pr.apply(env));
		env = env.extend(p1, Boolean.FALSE);
		assertEquals(false, pr.apply(env));
		env = env.extend(p2, Boolean.TRUE);
		assertEquals(false, pr.apply(env));
		env = env.extend(p2, Boolean.FALSE);
		assertEquals(true, pr.apply(env));
		env = env.filter(parameter -> !parameter.getName().equals("p1"));
		assertEquals(false, pr.apply(env));
	}

	@Test
	public void testToStringAndEquals() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		TemplateParameter p1 = b.addParameter(Type.Flag, "p1", Boolean.TRUE,
				Forward.Always, null);
		TemplateParameter p2 = b.addParameter(Type.Flag, "p2", null, Forward.Always, null);

		RhsPredicate pr = not(b, and(b,
				or(b, equals(b, p1, Boolean.TRUE), equals(b, p1, Boolean.FALSE)),
				equals(b, p2, Boolean.FALSE)));
		assertEquals("!((p1 == true || p1 == false) && p2 == false)", pr.toString());

		RhsPredicate identical = not(b, and(b,
				or(b, equals(b, p1, Boolean.TRUE), equals(b, p1, Boolean.FALSE)),
				equals(b, p2, Boolean.FALSE)));
		assertTrue(pr.equals(identical));
	}

	private static RhsPredicate equals(GrammarBuilder b, TemplateParameter param, Object value) {
		return b.predicate(Operation.Equals, null, param, value, null);
	}

	private static RhsPredicate and(GrammarBuilder b, RhsPredicate... predicates) {
		return b.predicate(Operation.And, Arrays.asList(predicates), null, null, null);
	}

	private static RhsPredicate or(GrammarBuilder b, RhsPredicate... predicates) {
		return b.predicate(Operation.Or, Arrays.asList(predicates), null, null, null);
	}

	private static RhsPredicate not(GrammarBuilder b, RhsPredicate predicate) {
		return b.predicate(Operation.Not, Collections.singleton(predicate), null, null, null);
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testDuplicateParameter() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("name `p1' is already used");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Flag, "p1", Boolean.TRUE, Forward.Always, null);
		builder.addParameter(Type.Flag, "p1", Boolean.TRUE, Forward.Always, null);
	}

	@Test
	public void testParameterAfterSymbol() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("name `p1' is already used");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addTerminal("p1", null, null);
		builder.addParameter(Type.Flag, "p1", Boolean.TRUE, Forward.Always, null);
	}

	@Test
	public void testSymbolAfterParameter() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("name `p1' is already used");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Flag, "p1", Boolean.TRUE, Forward.Always, null);
		builder.addTerminal("p1", null, null);
	}

	@Test
	public void testBadName() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("error");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Flag, "error", Boolean.TRUE, Forward.Always, null);
	}

	@Test
	public void testNullName() {
		expectedEx.expect(NullPointerException.class);
		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Flag, null, Boolean.TRUE, Forward.Always, null);
	}

	@Test
	public void testBadBoolValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("boolean default value is expected");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Flag, "error1", 1, Forward.Always, null);
	}

	@Test
	public void testBadSymbolValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("unknown symbol passed");

		GrammarBuilder builder1 = GrammarFacade.createBuilder();
		GrammarBuilder builder2 = GrammarFacade.createBuilder();
		builder1.addParameter(Type.Symbol, "int",
				builder2.addTerminal("bb", null, null), Forward.Always, null);
	}

	@Test
	public void testBadPredicate() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("inner");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.predicate(Operation.And, Collections.<RhsPredicate>emptyList(), null, null, null);
	}

	@Test
	public void testNoValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("boolean default value is expected");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		TemplateParameter p1 = builder.addParameter(Type.Flag, "p1", Boolean.TRUE,
				Forward.Always, null);
		builder.predicate(Operation.Equals, null, p1, null, null);
	}
}
