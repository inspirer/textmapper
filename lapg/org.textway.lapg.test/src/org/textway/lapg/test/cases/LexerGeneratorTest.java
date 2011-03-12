/**
 * Copyright 2002-2010 Evgeny Gryaznov
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

import junit.framework.TestCase;

import org.junit.Assert;

import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Lexem;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.test.TestStatus;

public class LexerGeneratorTest extends TestCase {

	TestLexem[] INPUT1 = {
			new TestLexem(0, 0, "string", "[a-z][A-Z]?", "bC", "aZ", "zA", "q"),
			new TestLexem(1, 0, "number", "[0-9]+", "1", "12", "323", "2111111"),
			new TestLexem(2, 0, "hex", "0x[0-9a-zA-Z]+", "0x23bd", "0x1", "0x0"),
			new TestLexem(3, 0, "hex", "\\n|\\t|\\r", "\n", "\t", "\r"),
			new TestLexem(4, 0, "hex", "[\\xAAAA-\\xAABB]", "\uaab0", "\uaabb", "\uaaaa", "\uaaaf"),
	};

	TestLexem[] ERRINPUT = {
			new TestLexem(0, 0, "string", "[a-z0-9][A-Z]?"),
			new TestLexem(1, 0, "number", "[0-9]+"),
			new TestLexem(2, 0, "empty", "()"),
	};

	public void testGenerator() {
		LexerTables lt = LexicalBuilder.compile(INPUT1, new TestStatus());
		for(TestLexem tl : INPUT1) {
			for(String s : tl.getSamples()) {
				int res = nextToken(lt, s);
				Assert.assertEquals("For "+s+" Expected " + tl.getRegexp()+ ";",tl.index, res);
			}
		}
	}

	public void testLexGeneratorReporting() {
		TestStatus notifier = new TestStatus(
				"",
				"lexemtest,3: empty: lexem is empty\n" +
				"lexemtest,1: two lexems are identical: string and number\n");
		LexicalBuilder.compile(ERRINPUT, notifier);
		notifier.assertDone();

	}

	private int nextToken(LexerTables lr, String s) {
		int state = 0;
		int index = 0;

		while(state >= 0) {
			int chr = index < s.length() ? s.codePointAt(index++) : 0;
			state = lr.change[state][chr>=0 && chr < lr.char2no.length ? lr.char2no[chr]: 1];
		}
		if( state == -1 ) {
			return -1;
		}
		if(state == -2) {
			return 0;
		}
		return lr.lnum[-state-3];
	}

	private static class TestLexem implements Lexem {

		private final int index;
		private final int prio;
		private final String name;
		private final String regexp;
		private final String[] samples;

		public TestLexem(int index, int prio, String name, String regexp, String ... samples) {
			this.index = index;
			this.prio = prio;
			this.name = name;
			this.regexp = regexp;
			this.samples = samples;
		}

		public SourceElement getAction() {
			return null;
		}

		public int getGroups() {
			return 1;
		}

		public int getPriority() {
			return prio;
		}

		public String getRegexp() {
			return regexp;
		}

		public String[] getSamples() {
			return samples;
		}

		public int getIndex() {
			return index;
		}

		public Symbol getSymbol() {
			return new Symbol() {
				public boolean isTerm() {
					throw new UnsupportedOperationException();
				}

				public boolean isDefined() {
					throw new UnsupportedOperationException();
				}

				public String getType() {
					throw new UnsupportedOperationException();
				}

				public String getName() {
					return name;
				}

                public String getId() {
                    return name;
                }

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
					return null;  //To change body of implemented methods use File | Settings | File Templates.
				}

				public int getOffset() {
					return 0;
				}

				public String getResourceName() {
					return null;
				}
			};
		}

		public int getEndOffset() {
			return 0;
		}

		public int getLine() {
			return index + 1;
		}

		public String getText() {
			return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

		public int getOffset() {
			return 0;
		}

		public String getResourceName() {
			return "lexemtest";
		}
	}

}
