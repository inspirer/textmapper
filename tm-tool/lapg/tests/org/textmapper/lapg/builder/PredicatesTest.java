/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.TemplateParameter;
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
		TemplateParameter s1 = b.addParameter(Type.String, "s1", "abc", null);
		TemplateParameter s2 = b.addParameter(Type.String, "s2", null, null);
		TemplateEnvironment env = GrammarFacade.createBuilder().getRootEnvironment();

		// With default value
		assertEquals("abc", env.getValue(s1));
		env = env.extend(s1, "qwe");
		assertEquals("qwe", env.getValue(s1));
		env = env.extend(s1, null);
		assertEquals("abc", env.getValue(s1));
		env = env.extend(s1, "");
		assertEquals("", env.getValue(s1));

		// With default value
		assertEquals(null, env.getValue(s2));
		env = env.extend(s2, "qwe");
		assertEquals("qwe", env.getValue(s2));
		env = env.extend(s2, null);
		assertEquals(null, env.getValue(s2));
		env = env.extend(s2, "");
		assertEquals("", env.getValue(s2));
	}

	@Test
	public void testEvaluation() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		TemplateParameter p1 = b.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
		TemplateParameter p2 = b.addParameter(Type.Bool, "p2", null, null);

		RhsPredicate pr = and(b, not(b, equals(b, p1, Boolean.TRUE)), equals(b, p2, Boolean.FALSE));
		assertEquals("!(p1 == true) && p2 == false", pr.toString());

		TemplateEnvironment env = b.getRootEnvironment();
		assertEquals(false, pr.apply(env));
		env = env.extend(p1, Boolean.FALSE);
		assertEquals(false, pr.apply(env));
		env = env.extend(p2, Boolean.TRUE);
		assertEquals(false, pr.apply(env));
		env = env.extend(p2, Boolean.FALSE);
		assertEquals(true, pr.apply(env));
		env = env.extend(p1, null);
		assertEquals(false, pr.apply(env));
	}

	@Test
	public void testToStringAndEquals() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		TemplateParameter p1 = b.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
		TemplateParameter p2 = b.addParameter(Type.Bool, "p2", null, null);

		RhsPredicate pr = not(b, and(b, or(b, equals(b, p1, Boolean.TRUE), equals(b, p1, Boolean.FALSE)),
				equals(b, p2, Boolean.FALSE)));
		assertEquals("!((p1 == true || p1 == false) && p2 == false)", pr.toString());

		RhsPredicate identical = not(b, and(b, or(b, equals(b, p1, Boolean.TRUE), equals(b, p1, Boolean.FALSE)),
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
		expectedEx.expectMessage("symbol (or template parameter) `p1' already exists");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
		builder.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
	}

	@Test
	public void testParameterAfterSymbol() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("symbol (or template parameter) `p1' already exists");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addTerminal("p1", null, null);
		builder.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
	}

	@Test
	public void testSymbolAfterParameter() {
		expectedEx.expect(IllegalStateException.class);
		expectedEx.expectMessage("symbol (or template parameter) `p1' already exists");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
		builder.addTerminal("p1", null, null);
	}

	@Test
	public void testBadName() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("error");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Bool, "error", Boolean.TRUE, null);
	}

	@Test
	public void testNullName() {
		expectedEx.expect(NullPointerException.class);
		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Bool, null, Boolean.TRUE, null);
	}

	@Test
	public void testBadBoolValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("boolean default value is expected");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Bool, "error1", 1, null);
	}

	@Test
	public void testBadIntValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("integer default value is expected");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.Integer, "int", "aaa", null);
	}

	@Test
	public void testBadStringValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("string default value is expected");

		GrammarBuilder builder = GrammarFacade.createBuilder();
		builder.addParameter(Type.String, "int", Boolean.TRUE, null);
	}

	@Test
	public void testBadSymbolValue() {
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("unknown symbol passed");

		GrammarBuilder builder1 = GrammarFacade.createBuilder();
		GrammarBuilder builder2 = GrammarFacade.createBuilder();
		builder1.addParameter(Type.Symbol, "int", builder2.addTerminal("bb", null, null), null);
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
		TemplateParameter p1 = builder.addParameter(Type.Bool, "p1", Boolean.TRUE, null);
		builder.predicate(Operation.Equals, null, p1, null, null);
	}

}
