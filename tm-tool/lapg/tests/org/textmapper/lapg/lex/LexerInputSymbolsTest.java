/**
 * Copyright 2002-2016 Evgeny Gryaznov
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

import org.junit.Test;
import org.textmapper.lapg.common.CharacterSetImpl;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LexerInputSymbolsTest {

	@Test
	public void testEmpty() throws Exception {
		LexerInputSymbols is = new LexerInputSymbols();
		assertEquals(0, is.getSetToSymbolsMap().length);

		int[] expected = new int[] {};
		assertArrayEquals(expected, is.getCharacterMap());

		assertEquals(2, is.getSymbolCount());
	}

	@Test
	public void testOneCharacter() throws Exception {
		LexerInputSymbols is = new LexerInputSymbols();
		is.addCharacter(1);
		assertEquals(0, is.getSetToSymbolsMap().length);

		int[] expected = new int[] {1, 2};
		assertArrayEquals(expected, is.getCharacterMap());

		assertEquals(3, is.getSymbolCount());
	}

	@Test
	public void testStates() throws Exception {
		LexerInputSymbols is = new LexerInputSymbols();
		assertEquals(0, is.getSetToSymbolsMap().length);

		try {
			is.addCharacter(1);
			fail();
		} catch (IllegalStateException ex) {
			/* good */
		}

		try {
			is.addSet(new CharacterSetImpl());
			fail();
		} catch (IllegalStateException ex) {
			/* good */
		}
	}

	@Test
	public void testAddCharacterInput() throws Exception {
		assertEquals(-1, new LexerInputSymbols().addCharacter(LexerInputSymbols.MAX_UCHAR + 1));
		assertEquals(0, new LexerInputSymbols().addCharacter(-1));
		assertEquals(-1, new LexerInputSymbols().addCharacter(Integer.MAX_VALUE));
		assertEquals(-1, new LexerInputSymbols().addCharacter(Integer.MIN_VALUE));
		assertEquals(2, new LexerInputSymbols().addCharacter(LexerInputSymbols.MAX_UCHAR));
		assertEquals(2, new LexerInputSymbols().addCharacter(0));
		assertEquals(2, new LexerInputSymbols().addCharacter(1));
	}
}
