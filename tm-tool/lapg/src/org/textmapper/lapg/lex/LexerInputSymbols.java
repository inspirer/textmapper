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
package org.textmapper.lapg.lex;

import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.common.CharacterSetImpl;

import java.util.*;

/**
 * evgeny, 6/2/12
 */
public class LexerInputSymbols {

	public static final int MAX_UCHAR = 0x10ffff;

	// result
	private int[] character2symbol;
	private int symbolCount;
	private final List<CharacterSet> setpool;
	private int[][] set2symbols;

	private CharacterSetImpl.Builder builder;
	private boolean sealed;

	public LexerInputSymbols() {
		this.builder = new CharacterSetImpl.Builder();
		this.character2symbol = new int[128];
		this.setpool = new ArrayList<CharacterSet>();

		// 0 - eoi, 1 - any
		Arrays.fill(character2symbol, 1);
		character2symbol[0] = 0;
		symbolCount = 2;
		sealed = false;
	}

	public int addSet(CharacterSet set) {
		if (sealed) {
			throw new IllegalStateException();
		}
		int setIndex = setpool.size();
		setpool.add(set);
		return setIndex;
	}

	public int addCharacter(int ch) {
		if (sealed) {
			throw new IllegalStateException();
		}
		if (ch <= 0 || ch > MAX_UCHAR) {
			return -1;
		}
		ensureCharacter(ch);
		if (character2symbol[ch] == 1) {
			character2symbol[ch] = symbolCount++;
		}
		return character2symbol[ch];
	}

	private void ensureCharacter(int ch) {
		assert ch >= 0 && ch <= MAX_UCHAR;
		if (character2symbol.length <= ch) {
			int newsize = character2symbol.length * 2;
			while (newsize <= ch) {
				newsize *= 2;
			}
			if (newsize > MAX_UCHAR + 1) {
				newsize = MAX_UCHAR + 1;
			}
			int[] newc2sym = new int[newsize];
			Arrays.fill(newc2sym, character2symbol.length, newc2sym.length, 1);
			System.arraycopy(character2symbol, 0, newc2sym, 0, character2symbol.length);
			character2symbol = newc2sym;
		}
	}

	private void buildSets() {
		assert !sealed;
		sealed = true;
		int base = symbolCount;
		List<CharacterSet> symbol2chars = new ArrayList<CharacterSet>();
		Set<Integer> values = new HashSet<Integer>();

		for (int setind = 0; setind < setpool.size(); setind++) {
			CharacterSet set = setpool.get(setind);
			int ownSymbol = -1;
			values.clear();
			builder.clear();
			for (int[] range : set) {
				int max = Math.min(range[1], MAX_UCHAR);
				ensureCharacter(max);
				for (int ch = range[0]; ch <= max; ch++) {
					int value = character2symbol[ch];
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
			for (int e : toSortedArray(values)) {
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
			set2symbols[setind] = toSortedArray(values);
		}
		builder = null;
	}

	private static int[] toSortedArray(Collection<Integer> collection) {
		int[] sortedValues = new int[collection.size()];
		int index = 0;
		for (Integer i : collection) {
			sortedValues[index++] = i;
		}
		assert index == collection.size();
		Arrays.sort(sortedValues);
		return sortedValues;
	}

	public int[][] getSetToSymbolsMap() {
		if (!sealed) {
			buildSets();
		}
		return set2symbols;
	}

	public int[] getCharacterMap() {
		if (!sealed) {
			buildSets();
		}
		return character2symbol;
	}

	public int getSymbolCount() {
		if (!sealed) {
			buildSets();
		}
		return symbolCount;
	}
}
