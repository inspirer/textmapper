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
package org.textway.lapg.lex;

import org.textway.lapg.api.regex.*;

import java.util.Map;
import java.util.Stack;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexpCompiler {

	// global vars
	private final Map<String, RegexPart> namedPatterns;
	private final LexerInputSymbols inputSymbols;

	// temporary variables
	private final int[] sym;
	private final Stack<Integer> stack;

	public RegexpCompiler(Map<String, RegexPart> namedPatterns) {
		this.namedPatterns = namedPatterns;

		this.sym = new int[1024];
		this.stack = new Stack<Integer>();
		this.inputSymbols = new LexerInputSymbols();
	}

	/**
	 * @return Engine representation of regular expression
	 */
	public int[] compile(int number, RegexPart regex) throws RegexpParseException {
		RegexpBuilder builder = new RegexpBuilder();
		try {
			regex.accept(builder);
		} catch (IllegalArgumentException ex) {
			throw new RegexpParseException(ex.getMessage(), 0);
		}


		int length = builder.getLength();
		sym[++length] = (LexConstants.DONE | number);

		int[] compiled = new int[length + 1];
		System.arraycopy(sym, 0, compiled, 0, length + 1);
		return compiled;
	}

	public LexerInputSymbols getInputSymbols() {
		return inputSymbols;
	}

	private class RegexpBuilder extends RegexVisitor {

		int length = -1;
		RegexOr outermostOr;

		public RegexpBuilder() {
		}

		public int getLength() {
			return length;
		}

		private void yield(int i) {
			sym[++length] = i;
		}

		@Override
		public void visit(RegexAny c) {
			inputSymbols.addCharacter('\n');
			yield(LexConstants.ANY);
		}

		@Override
		public void visit(RegexChar c) {
			yield(inputSymbols.addCharacter(c.getChar()) | LexConstants.SYM);
		}

		@Override
		public void visit(RegexExpand c) {
			String name = c.getName();
			RegexPart inner = namedPatterns.get(name);
			if (inner == null) {
				throw new IllegalArgumentException("cannot expand {" + c.getName() + "}, not found");
			}
			inner.accept(this);
		}

		@Override
		public void visitBefore(RegexList c) {
			if (c.isInParentheses()) {
				yield(LexConstants.LBR);
				stack.push(length);
			}
		}

		@Override
		public void visitAfter(RegexList c) {
			if (c.isInParentheses()) {
				yield(LexConstants.RBR);
				Integer left = stack.pop();
				sym[left] |= length;
			}
		}

		@Override
		public void visitBefore(RegexOr c) {
			if (length == -1) {
				outermostOr = c;
				yield(LexConstants.LBR);
			}
		}

		@Override
		public void visitBetween(RegexOr c) {
			yield(LexConstants.OR);
		}

		@Override
		public void visitAfter(RegexOr c) {
			if (outermostOr == c) {
				yield(LexConstants.RBR);
				sym[0] |= length;
			}
		}

		@Override
		public void visitBefore(RegexQuantifier c) {
		}

		@Override
		public void visitAfter(RegexQuantifier c) {
			if (c.getMin() == 0 && c.getMax() == 1) {
				yield(LexConstants.QMARK);
			} else if (c.getMin() == 0 && c.getMax() == -1) {
				yield(LexConstants.STAR);
			} else if (c.getMin() == 1 && c.getMax() == -1) {
				yield(LexConstants.PLUS);
			} else {
				throw new IllegalArgumentException("unsupported quantifier: " + c.toString());
			}
		}

		@Override
		public boolean visit(RegexSet c) {
			yield(LexConstants.SET | inputSymbols.addSet(c.getSet()));
			return false;
		}
	}
}
