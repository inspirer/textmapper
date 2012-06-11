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
package org.textway.lapg.regex;

import org.textway.lapg.api.regex.*;

import java.util.*;

public class RegexMatcher {

	private final RegexPart regex;
	private State[] states;

	public RegexMatcher(RegexPart regex, RegexContext context) {
		this.regex = regex;
		compile(context);
	}

	private void compile(RegexContext context) {
		RegexpBuilder builder = new RegexpBuilder(context);
		regex.accept(builder);
		states = builder.getResult();
	}

	public boolean matches(String text) {
		boolean[][] holders = new boolean[2][];
		holders[0] = new boolean[states.length];
		holders[1] = new boolean[states.length];

		int index = 0;
		boolean[] current = holders[0];
		states[0].applyTo(current);
		for (char c : text.toCharArray()) {
			boolean[] next = holders[++index % 2];
			Arrays.fill(next, false);
			boolean isValid = false;
			for (int i = 0; i < states.length; i++) {
				if (current[i] && states[i].simplePart != null && accepts(states[i].simplePart, c)) {
					states[i + 1].applyTo(next);
					isValid = true;
				}
			}
			if (!isValid) {
				return false;
			}
			current = next;
		}
		return current[states.length - 1];
	}

	private boolean accepts(RegexPart simple, char c) {
		if (simple instanceof RegexChar) {
			return c == ((RegexChar) simple).getChar();
		} else if (simple instanceof RegexSet) {
			return ((RegexSet) simple).getSet().contains(c);
		} else if (simple instanceof RegexAny) {
			return c != '\n';
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

	private static class StackElement {
		final int index;
		List<Integer> orlocations;

		private StackElement(int index) {
			this.index = index;
		}

		void addOr(int index) {
			if (orlocations == null) orlocations = new LinkedList<Integer>();
			orlocations.add(index);
		}

		void done(List<State> states) {
			if (orlocations != null) {
				for (int i : orlocations) {
					states.get(i).addJump(states.size());
				}
			}
		}
	}

	private static class RegexpBuilder extends RegexVisitor {

		private List<State> states = new ArrayList<State>();
		private Stack<StackElement> stack = new Stack<StackElement>();
		private RegexOr outermostOr;
		private final RegexContext context;

		public RegexpBuilder(RegexContext context) {
			this.context = context;
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
		public void visit(RegexAny c) {
			yield(c);
		}

		@Override
		public void visit(RegexChar c) {
			yield(c);
		}

		@Override
		public boolean visit(RegexSet c) {
			yield(c);
			return false;
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
				stack.push(new StackElement(index()));
				yield(null);    /* ( */
			}
		}

		@Override
		public void visitAfter(RegexList c) {
			if (c.isInParentheses()) {
				yield(null);    /* ) */
				stack.pop().done(states);
			}
		}

		@Override
		public void visitBefore(RegexOr c) {
			if (states.size() == 0) {
				outermostOr = c;
				stack.push(new StackElement(index()));
				yield(null);    /* ( */
			}
		}

		@Override
		public void visitBetween(RegexOr c) {
			stack.peek().addOr(index());
			states.get(stack.peek().index).addJump(index() + 1);
			yield(null);    /* | */
		}

		@Override
		public void visitAfter(RegexOr c) {
			if (outermostOr == c) {
				yield(null);    /* ) */
				stack.pop().done(states);
			}
		}

		@Override
		public void visitBefore(RegexQuantifier c) {
			stack.push(new StackElement(index()));
		}

		@Override
		public void visitAfter(RegexQuantifier c) {
			int start = stack.pop().index;
			if (c.getMin() == 0 && c.getMax() == 1) {
				states.get(start).addJump(index());
			} else if (c.getMin() == 0 && c.getMax() == -1) {
				states.get(start).addJump(index());
				yield(null);    /* splitter */
				states.get(index() - 1).addJump(start);
			} else if (c.getMin() == 1 && c.getMax() == -1) {
				yield(null);   /* splitter */
				states.get(index() - 1).addJump(start);
			} else {
				throw new IllegalArgumentException("unsupported quantifier: " + c.toString());
			}
		}
	}
}
