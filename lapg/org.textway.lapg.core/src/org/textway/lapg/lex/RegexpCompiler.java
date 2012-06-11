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
import org.textway.lapg.regex.RegexParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexpCompiler {

	// global vars
	private final RegexContext context;
	private final LexerInputSymbols inputSymbols;

	// temporary variables
	private final List<RegexInstruction> result;
	private final Stack<Integer> stack;

	public RegexpCompiler(RegexContext context) {
		this.context = context;
		this.result = new ArrayList<RegexInstruction>(256);
		this.stack = new Stack<Integer>();
		this.inputSymbols = new LexerInputSymbols();
	}

	/**
	 * @return Engine representation of regular expression
	 */
	public RegexInstruction[] compile(int number, RegexPart regex) throws RegexParseException {
		RegexpBuilder builder = new RegexpBuilder();
		result.clear();
		try {
			regex.accept(builder);
		} catch (IllegalArgumentException ex) {
			throw new RegexParseException(ex.getMessage(), 0);
		}

		builder.yield(RegexInstructionKind.Done, number);
		RegexInstruction[] res = result.toArray(new RegexInstruction[result.size()]);
		result.clear();
		return res;
	}

	public LexerInputSymbols getInputSymbols() {
		return inputSymbols;
	}

	private class RegexpBuilder extends RegexVisitor {

		RegexOr outermostOr;

		public RegexpBuilder() {
		}

		public int getLength() {
			return result.size();
		}

		private void yield(RegexInstructionKind kind, int value) {
			result.add(new RegexInstruction(kind, value));
		}

		@Override
		public void visit(RegexAny c) {
			inputSymbols.addCharacter('\n');
			yield(RegexInstructionKind.Any, 0);
		}

		@Override
		public void visit(RegexChar c) {
			yield(RegexInstructionKind.Symbol, inputSymbols.addCharacter(c.getChar()));
		}

		@Override
		public void visit(RegexExpand c) {
			String name = c.getName();
			RegexPart inner = context.resolvePattern(name);
			if (inner == null) {
				throw new IllegalArgumentException("cannot expand {" + c.getName() + "}, not found");
			}
			inner.accept(this);
		}

		@Override
		public void visitBefore(RegexList c) {
			if (c.isInParentheses()) {
				stack.push(getLength());
				yield(RegexInstructionKind.LeftParen, 0);
			}
		}

		@Override
		public void visitAfter(RegexList c) {
			if (c.isInParentheses()) {
				Integer left = stack.pop();
				result.set(left, new RegexInstruction(RegexInstructionKind.LeftParen, getLength()));
				yield(RegexInstructionKind.RightParen, 0);
			}
		}

		@Override
		public void visitBefore(RegexOr c) {
			if (getLength() == 0) {
				outermostOr = c;
				yield(RegexInstructionKind.LeftParen, 0);
			}
		}

		@Override
		public void visitBetween(RegexOr c) {
			yield(RegexInstructionKind.Or, 0);
		}

		@Override
		public void visitAfter(RegexOr c) {
			if (outermostOr == c) {
				result.set(0, new RegexInstruction(RegexInstructionKind.LeftParen, getLength()));
				yield(RegexInstructionKind.RightParen, 0);
			}
		}

		@Override
		public void visitBefore(RegexQuantifier c) {
		}

		@Override
		public void visitAfter(RegexQuantifier c) {
			if (c.getMin() == 0 && c.getMax() == 1) {
				yield(RegexInstructionKind.Optional, 0);
			} else if (c.getMin() == 0 && c.getMax() == -1) {
				yield(RegexInstructionKind.ZeroOrMore, 0);
			} else if (c.getMin() == 1 && c.getMax() == -1) {
				yield(RegexInstructionKind.OneOrMore, 0);
			} else {
				throw new IllegalArgumentException("unsupported quantifier: " + c.toString());
			}
		}

		@Override
		public boolean visit(RegexSet c) {
			yield(RegexInstructionKind.Set, inputSymbols.addSet(c.getSet()));
			return false;
		}
	}

}
