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

import java.util.*;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexpCompiler {

	// result
	private int[] character2symbol;
	private int symbolCount;
	private final List<CharacterSet> setpool;
	private int[][] set2symbols;

	// global vars
	private final Map<String, RegexPart> namedPatterns;

	// temporary variables
	private final CharacterSet.Builder builder;
	private final int[] sym;
	private final int[] stack;

	public RegexpCompiler(Map<String, RegexPart> namedPatterns) {
		this.namedPatterns = namedPatterns;

		this.sym = new int[LexConstants.MAX_ENTRIES];
		this.stack = new int[LexConstants.MAX_DEEP];
		this.builder = new CharacterSet.Builder();

		this.character2symbol = new int[128];
		this.setpool = new ArrayList<CharacterSet>();

		// 0 - eof, 1 - any
		Arrays.fill(character2symbol, 1);
		character2symbol[0] = 0;
		symbolCount = 2;
	}

	private int storeSet(CharacterSet set) {
		int setIndex = setpool.size();
		setpool.add(set);
		return setIndex;
	}

	private void useCharacter(int ch) {
		if (ch <= 0 || ch >= 0x10000) {
			return;
		}
		ensureCharacter(ch);
		if (character2symbol[ch] == 1) {
			character2symbol[ch] = symbolCount++;
		}
	}

	private void ensureCharacter(int ch) {
		if (character2symbol.length <= ch) {
			int newsize = character2symbol.length * 2;
			while (newsize <= ch) {
				newsize *= 2;
			}
			int[] newc2sym = new int[newsize];
			Arrays.fill(newc2sym, character2symbol.length, newc2sym.length, 1);
			System.arraycopy(character2symbol, 0, newc2sym, 0, character2symbol.length);
			character2symbol = newc2sym;
		}
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
		sym[++length] = -1 - number;

		int[] compiled = new int[length + 1];
		System.arraycopy(sym, 0, compiled, 0, length + 1);
		return compiled;
	}

	private class RegexpBuilder extends RegexVisitor {

		int length = -1, deep = 1;
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
			useCharacter('\n');
			yield(LexConstants.ANY);
		}

		@Override
		public void visit(RegexChar c) {
			useCharacter(c.getChar());
			yield(character2symbol[c.getChar()] | LexConstants.SYM);
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
				stack[deep++] = length;
			}
		}

		@Override
		public void visitAfter(RegexList c) {
			if (c.isInParentheses()) {
				yield(LexConstants.RBR);
				sym[stack[--deep]] |= length;
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
				sym[length] |= 3 << 29;
			} else if (c.getMin() == 0 && c.getMax() == -1) {
				sym[length] |= 2 << 29;
				yield(LexConstants.SPL);
			} else if (c.getMin() == 1 && c.getMax() == -1) {
				sym[length] |= 1 << 29;
				yield(LexConstants.SPL);
			} else {
				throw new IllegalArgumentException("unsupported quantifier: " + c.toString());
			}
		}

		@Override
		public boolean visit(RegexSet c) {
			yield(storeSet(c.getSet()));
			return false;
		}
	}

	public void buildSets() {
		int base = symbolCount;
		ArrayList<CharacterSet> symbol2chars = new ArrayList<CharacterSet>();
		HashSet<Integer> values = new HashSet<Integer>();

		for (int setind = 0; setind < setpool.size(); setind++) {
			CharacterSet set = setpool.get(setind);
			int ownSymbol = -1;
			values.clear();
			builder.clear();
			for (int[] range : set) {
				for (int ch = range[0]; ch <= range[1]; ch++) {
					int value;
					try {
						value = character2symbol[ch];
					} catch (IndexOutOfBoundsException ex) {
						ensureCharacter(ch);
						value = character2symbol[ch];
					}
					if (value == 1) {
						if (ownSymbol == -1) {
							ownSymbol = symbolCount++;
						}
						character2symbol[ch] = ownSymbol;
						builder.addSymbol(ch);
					} else if (value >= base) {
						values.add(value);
					}
				}
			}
			if (ownSymbol >= base) {
				symbol2chars.add(builder.create());
			}
			for (Integer e : values) {
				CharacterSet mset = symbol2chars.get(e - base);
				CharacterSet extraItems = builder.subtract(mset, set);
				if (!extraItems.isEmpty()) {
					symbol2chars.set(e - base, extraItems);

					// new symbol for intersection
					CharacterSet intersection = builder.intersect(mset, set);
					ownSymbol = symbolCount++;
					symbol2chars.add(intersection);

					for (int[] range : intersection) {
						for (int ch = range[0]; ch <= range[1]; ch++) {
							character2symbol[ch] = ownSymbol;
						}
					}
				}
			}
		}

		set2symbols = new int[setpool.size()][];
		for (int setind = 0; setind < setpool.size(); setind++) {
			CharacterSet set = setpool.get(setind);
			values.clear();
			if (set.isInverted()) {
				for (int i = 1; i < symbolCount; i++) {
					values.add(i);
				}
				for (int[] range : set) {
					for (int ch = range[0]; ch <= range[1]; ch++) {
						values.remove(character2symbol[ch]);
					}
				}
			} else {
				for (int[] range : set) {
					for (int ch = range[0]; ch <= range[1]; ch++) {
						values.add(character2symbol[ch]);
					}
				}
			}
			set2symbols[setind] = new int[values.size()];
			Integer[] arr = values.toArray(new Integer[values.size()]);
			for (int l = 0; l < set2symbols[setind].length; l++) {
				set2symbols[setind][l] = arr[l];
			}
			Arrays.sort(set2symbols[setind]);
		}
	}

	public int[][] getSetToSymbolsMap() {
		return set2symbols;
	}

	public int[] getCharacterMap() {
		return character2symbol;
	}

	public int getSymbolCount() {
		return symbolCount;
	}
}
