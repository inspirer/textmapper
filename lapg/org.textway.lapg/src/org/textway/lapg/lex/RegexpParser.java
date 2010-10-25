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
package org.textway.lapg.lex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RegexpParser {

	// result
	private int[] character2symbol;
	private int symbolCount;
	private final List<CharacterSet> setpool;
	private int[][] set2symbols;

	// temporary variables
	private final CharacterSet.Builder builder;
	private final int[] sym;
	private final int[] stack;

	public RegexpParser() {
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

	private int index;
	private char[] re;
	private String regexp;

	public static int parseHex(String s) {
		int result = 0;
		for (int i = 0; i < s.length(); i++) {
			result <<= 4;
			int c = s.codePointAt(i);
			if (c >= 'a' && c <= 'f') {
				result |= 10 + c - 'a';
			} else if (c >= 'A' && c <= 'F') {
				result |= 10 + c - 'A';
			} else if (c >= '0' && c <= '9') {
				result |= c - '0';
			} else {
				throw new NumberFormatException();
			}
		}
		return result;
	}

	/**
	 * @return unicode character 0 - 0xffff, or -1 in case of error
	 */
	private int escape() throws RegexpParseException {
		index++;
		if (index >= re.length) {
			throw new RegexpParseException("regexp contains \\ at the end of expression", re.length);
		}

		int c = re[index];
		switch (c) {
		case 'a':
			return 7;
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		case 'u':
		case 'x': {
			if (index + 4 >= re.length) {
				throw new RegexpParseException("regexp contains incomplete unicode symbol", re.length);
			}
			index += 4;
			try {
				return parseHex(new String(re, index - 3, 4));
			} catch (NumberFormatException ex) {
				throw new RegexpParseException("regexp contains incomplete unicode symbol", index-3);
			}
		}

		default:
			return c;
		}
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
	 * regular expression "tokenizer"
	 */
	private int getnext() throws RegexpParseException {
		int i, e;
		boolean invert = false;

		switch (re[index]) {
		case '(':
			index++;
			return LexConstants.LBR;

		case ')':
			index++;
			return LexConstants.RBR;

		case '|':
			index++;
			return LexConstants.OR;

		case '.':
			index++;
			useCharacter('\n');
			return LexConstants.ANY;

		case '[':
			index++;
			invert = index < re.length && re[index] == '^';
			if (invert) {
				index++;
			}

			builder.clear();

			while (index < re.length && re[index] != ']') {
				i = re[index];
				if (i == '\\') {
					i = escape();
					if (i == -1) {
						return -1;
					}
				}
				index++;
				if (index + 1 < re.length && re[index] == '-' && re[index + 1] != ']') {
					e = re[++index];
					if (e == '\\') {
						e = escape();
						if (e == -1) {
							return -1;
						}
					}

					if (e > i) {
						builder.addRange(i, e);
					} else {
						builder.addRange(e, i);
					}
					index++;
				} else {
					builder.addSymbol(i);
				}
			}

			if (index >= re.length) {
				throw new RegexpParseException("regexp contains unpaired brackets", re.length);
			}

			index++;
			return storeSet(builder.create(invert));

		case ']':
			throw new RegexpParseException("unexpected closing brace, escape it to use as character", index);

		case '\\':
			i = escape();
			if (i == -1) {
				return -1;
			}
			useCharacter(i);
			index++;
			return character2symbol[i] | LexConstants.SYM;

		default:
			i = re[index];
			useCharacter(i);
			index++;
			return character2symbol[i] | LexConstants.SYM;
		}
	}

	/**
	 * @return Engine representation of regular expression
	 */
	public int[] compile(int number, String name, String regexp) throws RegexpParseException {

		int length = 0, deep = 1;
		boolean addbrackets = false;

		this.index = 0;
		this.regexp = regexp;
		this.re = this.regexp.toCharArray();

		if (re.length == 0) {
			throw new RegexpParseException("regexp is empty", 0);
		}

		while (index < re.length) {
			if (length > LexConstants.MAX_ENTRIES - 5) {
				throw new RegexpParseException("regexp is too long", index);
			}

			sym[length] = getnext();

			switch (sym[length]) {
			case LexConstants.LBR:
				stack[deep] = length;
				if (++deep >= LexConstants.MAX_DEEP) {
					throw new RegexpParseException("regexp contains too much parentheses", index-1);
				}
				break;
			case LexConstants.OR:
				if (deep == 1) {
					addbrackets = true;
				}
				break;
			case LexConstants.RBR:
				if (--deep == 0) {
					throw new RegexpParseException("regexp contains unpaired parentheses", index-1);
				}
				sym[stack[deep]] |= length;
				/* FALLTHROUGH */
			default:
				if (index < re.length && (re[index] == '+' || re[index] == '?' || re[index] == '*')) {
					switch (re[index]) {
					case '+':
						sym[length] |= 1 << 29;
						break;
					case '*':
						sym[length] |= 2 << 29;
						break;
					case '?':
						sym[length] |= 3 << 29;
						break;
					}
					if (re[index] != '?') {
						sym[++length] = LexConstants.SPL;
					}
					index++;
				}
				break;
			case -1:
				return null;
			}
			length++;
		}

		if (deep != 1) {
			throw new RegexpParseException("regexp contains unpaired parentheses", re.length);
		}

		int e;
		if (addbrackets) {
			for (e = 0; e < length; e++) {
				if ((sym[e] & LexConstants.MASK) == LexConstants.LBR) {
					sym[e] = (sym[e] & ~0xffff) | ((sym[e] & 0xffff) + 1);
				}
			}

			sym[length++] = LexConstants.RBR;
		}

		sym[length++] = -1 - number;

		e = 0;
		int[] result = new int[length + (addbrackets ? 1 : 0)];
		if (addbrackets) {
			result[e++] = LexConstants.LBR | (length - 1);
		}
		for (int i = 0; i < length; i++, e++) {
			result[e] = sym[i];
		}

		return result;
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
			for(int[] range : set) {
				for(int ch = range[0]; ch <= range[1]; ch++) {
					int value;
					try {
						value = character2symbol[ch];
					} catch(IndexOutOfBoundsException ex) {
						ensureCharacter(ch);
						value = character2symbol[ch];
					}
					if(value == 1) {
						if(ownSymbol == -1) {
							ownSymbol = symbolCount++;
						}
						character2symbol[ch] = ownSymbol;
						builder.addSymbol(ch);
					} else if(value >= base) {
						values.add(value);
					}
				}
			}
			if(ownSymbol >= base) {
				symbol2chars.add(builder.create());
			}
			for(Integer e : values) {
				CharacterSet mset = symbol2chars.get(e - base);
				CharacterSet extraItems = builder.subtract(mset, set);
				if(!extraItems.isEmpty()) {
					symbol2chars.set(e - base, extraItems);

					// new symbol for intersection
					CharacterSet intersection = builder.intersect(mset, set);
					ownSymbol = symbolCount++;
					symbol2chars.add(intersection);

					for(int[] range : intersection) {
						for(int ch = range[0]; ch <= range[1]; ch++) {
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
			if(set.isInverted()) {
				for(int i = 1; i < symbolCount; i++) {
					values.add(i);
				}
				for(int[] range : set) {
					for(int ch = range[0]; ch <= range[1]; ch++) {
						values.remove(character2symbol[ch]);
					}
				}
			} else {
				for(int[] range : set) {
					for(int ch = range[0]; ch <= range[1]; ch++) {
						values.add(character2symbol[ch]);
					}
				}
			}
			set2symbols[setind] = new int[values.size()];
			Integer[] arr = values.toArray(new Integer[values.size()]);
			for(int l = 0; l < set2symbols[setind].length; l++) {
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
