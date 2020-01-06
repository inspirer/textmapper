/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
package org.textmapper.lapg.lex;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.regex.RegexParseException;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.test.TestStatus;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LexerGeneratorTest {

	private final NamedPattern[] NO_PATTERNS = new NamedPattern[0];

	LexerState[] LEXER_STATES = {
			new TestLexerState(0, "initial")
	};

	TestRule[] INPUT1 = {
			new TestRule(0, 0, "string", LEXER_STATES[0], "[a-z][A-Z]?", "bC", "aZ", "zA", "q"),
			new TestRule(1, 0, "number", LEXER_STATES[0], "[0-9]+", "1", "12", "323", "2111111"),
			new TestRule(2, 0, "hex", LEXER_STATES[0], "0x[0-9a-zA-Z]+", "0x23bd", "0x1", "0x0"),
			new TestRule(3, 0, "hex", LEXER_STATES[0], "\\n|\\t|\\r", "\n", "\t", "\r"),
			new TestRule(4, 0, "hex", LEXER_STATES[0], "[\\uAAAA-\\uAABB]", "\uaab0", "\uaabb",
					"\uaaaa", "\uaaaf"),
	};

	TestRule[] ERRINPUT = {
			new TestRule(0, 0, "string", LEXER_STATES[0], "[a-z0-9][A-Z]?"),
			new TestRule(1, 0, "number", LEXER_STATES[0], "[0-9]+"),
			new TestRule(2, 0, "empty", LEXER_STATES[0], "()"),
	};

	@Test
	public void testSimple() {
		checkMatch("axy", "ayy", false);
		checkMatch("axy", "axy", true);
		checkMatch("abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz", true);
		checkMatch("\\u1234", "\u1234", true);
		checkMatch("(b)", "b", true);
		checkMatch("a(b)c", "abc", true);
		checkMatch("a(X|Y)c", "aYc", true);
		checkMatch("a(X|Y)c", "aXc", true);
		checkMatch("X|Y", "X", true);
		checkMatch("X|Y", "Y", true);
		checkMatch("X|Y", "Z", false);

		checkMatch("a(cz)+q", "aczq", true);
		checkMatch("a(cz)+q", "aczczq", true);
		checkMatch("a(cz)+q", "aczcq", false);

		// set
		checkMatch("[@]", "@", true);
		checkMatch("[^@]", "@", false);
		checkMatch("[^@]", "\u1234", true);

		// or
		checkMatch("a|ax", "ax", true);
		checkMatch("a|ax", "a", true);
		checkMatch("a|ax", "ay", false);
	}

	@Test
	public void testZeroCharacter() {
		// \0
		checkMatch("\\000", "\0", true);
		checkMatch("\\000", "\1", false);
		checkMatch("\\x00", "\0", true);
		checkMatch("\0|\1", "\0", true);
		checkMatch("\0|\1", "\1", true);
		checkMatch("\0|\1", "\2", false);
	}

	@Test
	public void testSpecialChars() {
		checkMatch("\\a", "\\a", false);
		checkMatch("\\a", "\007", true);
		checkMatch("\\b", "\b", true);
		checkMatch("\\b", "\\b", false);
		checkMatch("\\f", "\f", true);
		checkMatch("\\f", "\\f", false);
		checkMatch("\\f", "f", false);
		checkMatch("\\n", "\n", true);
		checkMatch("\\n", "\\", false);
		checkMatch("\\n", "\\n", false);
		checkMatch("\\n", "n", false);
		checkMatch("\\r", "\r", true);
		checkMatch("\\t", "\t", true);
		checkMatch("\\v", "\u000b", true);
	}

	@Test
	public void testQuantifiers() {
		checkMatch("lapg(T*)", "lapgTTTT", true);
		checkMatch("lapg(T*)", "prefixlapgTTTTTTTTT", false);
		checkMatch("lapg(T*)", "lapgTpostfix", false);
	}

	@Test
	public void testComplexQuantifiers() throws RegexParseException {
		// optional
		checkMatch("qa{0,1}", "q", true);
		checkMatch("qa{0,1}", "qa", true);
		checkMatch("qa{0,1}", "qaa", false);

		// upper bound
		checkMatch("a{0,7}z", "z", true);
		checkMatch("a{0,7}z", "az", true);
		checkMatch("a{0,7}z", "aaaaaaz", true);
		checkMatch("a{0,7}z", "aaaaaaaz", true);
		checkMatch("a{0,7}z", "aaaaaaaaz", false);

		// range
		checkMatch("a{6,9}", "aaaaa", false);
		checkMatch("a{6,9}", "aaaaaa", true);
		checkMatch("a{6,9}", "aaaaaaaaa", true);
		checkMatch("a{6,9}", "aaaaaaaaaa", false);

		// exact match
		checkMatch("[a-z]{4}", "aza", false);
		checkMatch("[a-z]{4}", "azaz", true);
		checkMatch("[a-z]{4}", "azazy", false);
	}

	@Test
	public void testUnicode() {
		for (int cp = 1; cp < 0x333; cp++) {
			String s = "L" + new String(Character.toChars(cp)) + "R";
			checkMatch("L" + String.format("\\u%04x", cp) + "R", s, true);
			checkMatch("L[" + String.format("\\u%04x", cp) + "]R", s, true);
			if (cp < 0xff) {
				checkMatch("L" + String.format("\\x%02x", cp) + "R", s, true);
				checkMatch("L[" + String.format("\\x%02x", cp) + "]R", s, true);
			}
		}
	}

	private void checkMatch(String regex, String sample, boolean expected) {
		TestRule[] input = {new TestRule(0, 0, "test", LEXER_STATES[0], regex)};
		LexerData lt = LexerGenerator.generate(LEXER_STATES, input, NO_PATTERNS, new TestStatus());
		int token = nextToken(lt, sample, input);
		assertEquals(sample + " !~ /" + regex, expected, token == 0);
	}

	@Test
	public void testGenerator() {
		LexerData lt = LexerGenerator.generate(LEXER_STATES, INPUT1, NO_PATTERNS, new TestStatus
				());
		for (TestRule tl : INPUT1) {
			for (String s : tl.getSamples()) {
				int res = nextToken(lt, s, INPUT1);
				assertEquals("For " + s + " Expected " + tl.getRegexp().toString() + ";", tl
						.index, res);
			}
		}
	}

	@Test
	public void testLexGeneratorReporting() {
		TestStatus notifier = new TestStatus(
				"",
				"lexergentest,3: `empty' accepts empty text\n" +
						"lexergentest,1: two rules are identical: string and number\n");
		LexerGenerator.generate(LEXER_STATES, ERRINPUT, NO_PATTERNS, notifier);
		notifier.assertDone();

	}

	/* returns token index if s matches regexp */
	private int nextToken(LexerData lr, String s, LexerRule[] lexerRules) {
		int state = 0;
		int index = 0;

		final int tmFirstRule = -1 - lr.getBacktracking().length / 2;
		while (state >= 0) {
			int chr = index < s.length() ? s.codePointAt(index) : 0;
			index++;
			state = lr.getChange()[state * lr.getNchars() + (chr >= 0 && chr < lr.getChar2no()
					.length ? lr.getChar2no()[chr] : 1)];
			if (state < 0 && state > tmFirstRule) {
				state = (-1 - state) * 2;
				// TODO test backtracking
				state = lr.getBacktracking()[state + 1];
			}
		}
		if (index != s.length() + 1) {
			return -1;
		}
		if (state == tmFirstRule || state == tmFirstRule - 1) {
			return -1;
		}
		return lexerRules[tmFirstRule - state - 2].getSymbol().getIndex();
	}

	private static class TestLexerState implements LexerState {

		final int index;
		final Name name;

		private TestLexerState(int index, String name) {
			this.index = index;
			this.name = LapgCore.name(name);
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public Name getName() {
			return name;
		}

		@Override
		public String getNameText() {
			return name.text();
		}

		@Override
		public Object getUserData(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putUserData(String key, Object value) {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestRule implements LexerRule, TextSourceElement {

		private final int index;
		private final int prio;
		private final Name name;
		private final LexerState initial;
		private final String regexp;
		private final String[] samples;

		public TestRule(int index, int prio, String name, LexerState initial, String regexp,
						String... samples) {
			this.index = index;
			this.prio = prio;
			this.name = LapgCore.name(name);
			this.initial = initial;
			this.regexp = regexp;
			this.samples = samples;
		}

		public SourceElement getAction() {
			return null;
		}

		@Override
		public int getKind() {
			return KIND_NONE;
		}

		@Override
		public String getKindAsText() {
			return "none";
		}

		@Override
		public LexerRule getClassRule() {
			return null;
		}

		@Override
		public boolean isExcluded() {
			return false;
		}

		@Override
		public int getPriority() {
			return prio;
		}

		@Override
		public Iterable<LexerState> getStates() {
			return Collections.singleton(initial);
		}

		@Override
		public RegexPart getRegexp() {
			try {
				return LapgCore.parse(getSymbol().getNameText(), regexp);
			} catch (RegexParseException ex) {
				fail(ex.toString());
				return null;
			}
		}

		public String[] getSamples() {
			return samples;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public Terminal getSymbol() {
			return new Terminal() {
				@Override
				public void setName(String name) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void updateNameHint(String nameHint) {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getNameHint() {
					return null;
				}

				@Override
				public boolean isTerm() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean isUnused() {
					return false;
				}

				@Override
				public AstType getType() {
					return null;
				}

				@Override
				public boolean isConstant() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getConstantValue() {
					return null;
				}

				@Override
				public Collection<LexerRule> getRules() {
					throw new UnsupportedOperationException();
				}

				@Override
				public Name getName() {
					return name;
				}

				@Override
				public String getNameText() {
					return name.text();
				}

				public String getId() {
					return name.text();
				}

				@Override
				public int getIndex() {
					return index;
				}

				public void addAnnotation(String name, Object value) {
					throw new UnsupportedOperationException();
				}

				public Object getAnnotation(String name) {
					return null;
				}

				public int getEndOffset() {
					return 0;
				}

				public int getLine() {
					return 0;
				}

				public String getText() {
					throw new UnsupportedOperationException();
				}

				public int getOffset() {
					return 0;
				}

				public String getResourceName() {
					return null;
				}

				@Override
				public Object getUserData(String key) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void putUserData(String key, Object value) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public int getEndoffset() {
			return 0;
		}

		@Override
		public int getLine() {
			return index + 1;
		}

		@Override
		public String getText() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getOffset() {
			return 0;
		}

		@Override
		public String getResourceName() {
			return "lexergentest";
		}

		@Override
		public Object getUserData(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putUserData(String key, Object value) {
			throw new UnsupportedOperationException();
		}
	}

}
