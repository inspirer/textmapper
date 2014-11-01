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
package org.textmapper.lapg.regex;

import org.textmapper.lapg.api.regex.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class RegexMatcherImpl implements RegexMatcher {

	private final RegexPart regex;
	private State[] states;

	RegexMatcherImpl(RegexPart regex, RegexContext context) throws RegexParseException {
		this.regex = regex;
		compile(context);
	}

	private void compile(RegexContext context) throws RegexParseException {
		RegexpBuilder builder = new RegexpBuilder(context);
		regex.accept(builder);
		if (builder.getErrorMessage() != null) {
			throw new RegexParseException(builder.getErrorMessage(), builder.getErrorOffset());
		}
		states = builder.getResult();
	}

	private boolean apply(int c, boolean[] current, boolean[] next) {
		Arrays.fill(next, false);
		boolean isValid = false;
		for (int i = 0; i < states.length; i++) {
			if (current[i] && states[i].simplePart != null && accepts(states[i].simplePart, c)) {
				states[i + 1].applyTo(next);
				isValid = true;
			}
		}
		return isValid;
	}

	public boolean matches(String text) {
		boolean[][] holders = new boolean[2][];
		holders[0] = new boolean[states.length];
		holders[1] = new boolean[states.length];

		int index = 0;
		boolean[] current = holders[0];
		states[0].applyTo(current);
		char[] input = text.toCharArray();
		for (int e = 0; e < input.length; e++) {
			int c = input[e];
			if (Character.isHighSurrogate(input[e]) && e + 1 < input.length) {
				c = Character.toCodePoint(input[e++], input[e]);
			}
			boolean[] next = holders[++index % 2];
			if (!apply(c, current, next)) return false;
			current = next;
		}
		while (!current[states.length - 1]) {
			boolean[] next = holders[++index % 2];
			if (!apply(-1, current, next)) return false;
			current = next;
		}
		return true;
	}

	private boolean accepts(RegexPart simple, int c) {
		if (simple instanceof RegexChar) {
			return c == ((RegexChar) simple).getChar();
		} else if (simple instanceof RegexSet) {
			return c >= 0 && ((RegexSet) simple).getSet().contains(c);
		} else if (simple instanceof RegexAny) {
			return c != -1 && c != '\n';
		}
		return false;
	}

	@Override
	public String toString() {
		return regex.toString();
	}

	private static class State {
		int index;
		List<Integer> jumps;
		RegexPart simplePart;   /* any, char, charclass, set */
		boolean[] closure;

		void addJump(int target) {
			if (jumps == null) jumps = new LinkedList<Integer>();
			jumps.add(target);
		}

		void allocate(int size) {
			closure = new boolean[size];
			closure[index] = true;
			if (jumps == null) return;
			for (int i : jumps) {
				closure[i] = true;
			}
			jumps = null;
		}

		void applyTo(boolean[] target) {
			for (int i = 0; i < target.length; i++) {
				target[i] |= closure[i];
			}
		}
	}

	private static class RegexpBuilder extends RegexCompilingSwitch {

		private List<State> states = new ArrayList<State>();
		private final RegexContext context;
		private String errorMessage = null;
		private int errorOffset = -1;

		public RegexpBuilder(RegexContext context) {
			this.context = context;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public int getErrorOffset() {
			return errorOffset;
		}

		public State[] getResult() {
			State s = new State();
			s.index = states.size();
			states.add(s);
			return closure();
		}

		private State[] closure() {
			State[] states = this.states.toArray(new State[this.states.size()]);
			for (State s : states) {
				s.allocate(states.length);
			}
			// transitive closure of jumps
			int len = states.length;
			for (int i = 0; i < len; i++) {
				for (int j = 0; j < len; j++) {
					if (states[j].closure[i]) {
						for (int e = 0; e < len; e++) {
							if (states[i].closure[e]) {
								states[j].closure[e] = true;
							}
						}
					}
				}
			}
			return states;
		}

		private State yield(RegexPart simplepart) {
			State s = new State();
			s.index = states.size();
			if (simplepart != null) {
				s.simplePart = simplepart;
			} else {
				s.addJump(s.index + 1);
			}
			states.add(s);
			return s;
		}

		private int index() {
			return states.size();
		}

		@Override
		public Void caseAny(RegexAny c) {
			yield(c);
			return null;
		}

		@Override
		public Void caseChar(RegexChar c) {
			yield(c);
			return null;
		}

		@Override
		public Void caseSet(RegexSet c) {
			yield(c);
			return null;
		}

		@Override
		public Void caseExpand(RegexExpand c) {
			String name = c.getName();
			RegexPart inner = context.resolvePattern(name);
			if (inner == null) {
				errorMessage = "cannot expand {" + c.getName() + "}, not found";
				// TODO
				errorOffset = 0;
				return null;
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
			List<Integer> orlocations = new LinkedList<Integer>();
			int startIndex = index();
			yield(null);    /* ( */
			boolean first = true;
			for (RegexPart element : c.getVariants()) {
				if (!first) {
					orlocations.add(index());
					states.get(startIndex).addJump(index() + 1);
					yield(null);    /* | */
				} else {
					first = false;
				}
				element.accept(this);
			}
			yield(null);    /* ) */
			for (int i : orlocations) {
				states.get(i).addJump(index());
			}
			return null;
		}

		@Override
		public void yield(RegexPart part, boolean optional, boolean multiple) {
			int startIndex = index();
			part.accept(this);
			if (optional) {
				states.get(startIndex).addJump(index());
			}
			if (multiple) {
				yield(null);    /* splitter */
				states.get(index() - 1).addJump(startIndex);
			}
		}
	}
}
