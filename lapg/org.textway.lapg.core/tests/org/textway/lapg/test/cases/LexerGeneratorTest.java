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
package org.textway.lapg.test.cases;

import org.junit.Test;
import org.textway.lapg.api.*;
import org.textway.lapg.api.regex.RegexPart;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.lex.RegexMatcher;
import org.textway.lapg.lex.RegexpParseException;
import org.textway.lapg.api.TextSourceElement;
import org.textway.lapg.test.TestStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LexerGeneratorTest {

	private final NamedPattern[] NO_PATTERNS = new NamedPattern[0];

	TestLexem[] INPUT1 = {
			new TestLexem(0, 0, "string", "[a-z][A-Z]?", "bC", "aZ", "zA", "q"),
			new TestLexem(1, 0, "number", "[0-9]+", "1", "12", "323", "2111111"),
			new TestLexem(2, 0, "hex", "0x[0-9a-zA-Z]+", "0x23bd", "0x1", "0x0"),
			new TestLexem(3, 0, "hex", "\\n|\\t|\\r", "\n", "\t", "\r"),
			new TestLexem(4, 0, "hex", "[\\uAAAA-\\uAABB]", "\uaab0", "\uaabb", "\uaaaa", "\uaaaf"),
	};

	TestLexem[] ERRINPUT = {
			new TestLexem(0, 0, "string", "[a-z0-9][A-Z]?"),
			new TestLexem(1, 0, "number", "[0-9]+"),
			new TestLexem(2, 0, "empty", "()"),
	};

	@Test
	public void testGenerator() {
		LexerTables lt = LexicalBuilder.compile(INPUT1, NO_PATTERNS, new TestStatus());
		for (TestLexem tl : INPUT1) {
			for (String s : tl.getSamples()) {
				int res = nextToken(lt, s, INPUT1);
				assertEquals("For " + s + " Expected " + tl.getRegexp().toString() + ";", tl.index, res);
			}
		}
	}

	@Test
	public void testLexGeneratorReporting() {
		TestStatus notifier = new TestStatus(
				"",
				"lexemtest,3: empty: lexem is empty\n" +
						"lexemtest,1: two lexems are identical: string and number\n");
		LexicalBuilder.compile(ERRINPUT, NO_PATTERNS, notifier);
		notifier.assertDone();

	}

	private int nextToken(LexerTables lr, String s, Lexem[] lexems) {
		int state = 0;
		int index = 0;

		while (state >= 0) {
			int chr = index < s.length() ? s.codePointAt(index++) : 0;
			state = lr.getChange()[state * lr.getNchars() + (chr >= 0 && chr < lr.getChar2no().length ? lr.getChar2no()[chr] : 1)];
		}
		if (state == -1) {
			return -1;
		}
		if (state == -2) {
			return 0;
		}
		return lexems[-state - 3].getSymbol().getIndex();
	}

	private static class TestLexem implements Lexem, TextSourceElement {

		private final int index;
		private final int prio;
		private final String name;
		private final String regexp;
		private final String[] samples;

		public TestLexem(int index, int prio, String name, String regexp, String... samples) {
			this.index = index;
			this.prio = prio;
			this.name = name;
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
		public Lexem getClassLexem() {
			return null;
		}

		@Override
		public boolean isExcluded() {
			return false;
		}

		@Override
		public int getGroups() {
			return 1;
		}

		@Override
		public int getPriority() {
			return prio;
		}

		@Override
		public RegexPart getRegexp() {
			try {
				return RegexMatcher.parse(getSymbol().getName(), regexp);
			} catch (RegexpParseException ex) {
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
		public Symbol getSymbol() {
			return new Symbol() {
				@Override
				public boolean isTerm() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean isSoft() {
					throw new UnsupportedOperationException();
				}

				@Override
				public Symbol getSoftClass() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getType() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getName() {
					return name;
				}

				public String getId() {
					return name;
				}

				@Override
				public int getKind() {
					return KIND_TERM;
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
				public String kindAsString() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public int getEndOffset() {
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
			return "lexemtest";
		}
	}

}
