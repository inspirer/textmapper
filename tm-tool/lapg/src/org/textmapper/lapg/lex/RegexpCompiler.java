/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.lapg.api.regex.*;
import org.textmapper.lapg.regex.RegexCompilingSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexpCompiler {

	// global vars
	private final RegexContext context;
	private final LexerInputSymbols inputSymbols;

	// temporary variables
	private final List<RegexInstruction> result;

	public RegexpCompiler(RegexContext context) {
		this.context = context;
		this.result = new ArrayList<RegexInstruction>(256);
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

	private class RegexpBuilder extends RegexCompilingSwitch {

		public RegexpBuilder() {
		}

		public int getLength() {
			return result.size();
		}

		private void yield(RegexInstructionKind kind, int value) {
			result.add(new RegexInstruction(kind, value));
		}

		@Override
		public Void caseAny(RegexAny c) {
			inputSymbols.addCharacter('\n');
			yield(RegexInstructionKind.Any, 0);
			return null;
		}

		@Override
		public Void caseChar(RegexChar c) {
			yield(RegexInstructionKind.Symbol, inputSymbols.addCharacter(c.getChar()));
			return null;
		}

		@Override
		public Void caseExpand(RegexExpand c) {
			String name = c.getName();
			RegexPart inner = context.resolvePattern(name);
			if (inner == null) {
				throw new IllegalArgumentException("cannot expand {" + c.getName() + "}, not found");
			}
			inner.accept(this);
			return null;
		}

		@Override
		public Void caseList(RegexList c) {
			for (RegexPart e : c.getElements()) {
				e.accept(this);
			}
			return null;
		}

		@Override
		public Void caseOr(RegexOr c) {
			int start = getLength();
			yield(RegexInstructionKind.LeftParen, 0);
			boolean first = true;
			for (RegexPart element : c.getVariants()) {
				if (!first) {
					yield(RegexInstructionKind.Or, 0);
				} else {
					first = false;
				}
				element.accept(this);
			}
			result.set(start, new RegexInstruction(RegexInstructionKind.LeftParen, getLength()));
			yield(RegexInstructionKind.RightParen, 0);
			return null;
		}

		@Override
		public void yield(RegexPart part, boolean optional, boolean multiple) {
			int start = getLength();
			yield(RegexInstructionKind.LeftParen, 0);
			part.accept(this);
			result.set(start, new RegexInstruction(RegexInstructionKind.LeftParen, getLength()));
			yield(RegexInstructionKind.RightParen, 0);
			if (optional) {
				yield(multiple ? RegexInstructionKind.ZeroOrMore : RegexInstructionKind.Optional, 0);
			} else if (multiple) {
				yield(RegexInstructionKind.OneOrMore, 0);
			}
		}

		@Override
		public Void caseSet(RegexSet c) {
			yield(RegexInstructionKind.Set, inputSymbols.addSet(c.getSet()));
			return null;
		}
	}
}
