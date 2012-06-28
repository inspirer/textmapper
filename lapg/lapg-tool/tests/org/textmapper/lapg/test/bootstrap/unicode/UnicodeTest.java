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
package org.textmapper.lapg.test.bootstrap.unicode;

import org.junit.Test;
import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.test.bootstrap.unicode.UnicodeTestLexer.ErrorReporter;
import org.textmapper.lapg.test.bootstrap.unicode.UnicodeTestLexer.LapgSymbol;
import org.textmapper.lapg.test.bootstrap.unicode.UnicodeTestLexer.Lexems;
import org.textmapper.lapg.unicode.UnicodeData;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 2/21/12
 */
public class UnicodeTest {

	@Test
	public void testInts() {
		valid("12 23 123", Lexems.icon, Lexems.icon, Lexems.icon);
		valid("900000000", Lexems.icon);
		valid("\n\t 1 \t\n", Lexems.icon);
	}

	@Test
	public void testIds() {
		valid("_", Lexems.identifier);
		valid("a", Lexems.identifier);
		valid("aa12 23 zaAa_", Lexems.identifier, Lexems.icon, Lexems.identifier);
	}

	@Test
	public void testStrings() {
		valid("\"a\"", Lexems.string);
		CharacterSet Ll = UnicodeData.getCategory(Character.LOWERCASE_LETTER);
		for (int[] range : Ll) {
			for (int i = range[0]; i < range[1]; i++) {
				// TODO support > 0xffff
				if (i > 0xffff) return;
				assertTrue(Ll.contains(i));
				valid("\"" + Character.toString((char) i) + "\"", Lexems.string);
			}
		}
	}

	private void valid(String text, int... expectedLexems) {
		UnicodeTestLexer lexer;
		try {
			lexer = new UnicodeTestLexer(new StringReader(text), new ErrorReporter() {
				@Override
				public void error(int start, int line, String s) {
					fail("unexpected failure: " + start + ": " + s);
				}
			});
			LapgSymbol next;
			for (int i = 0; i < expectedLexems.length; i++) {
				int expected = expectedLexems[i];
				next = lexer.next();
				assertFalse(next.lexem == Lexems.eoi);
				assertEquals(expected, next.lexem);
			}
			next = lexer.next();
			assertTrue(next.lexem == Lexems.eoi);

		} catch (IOException e) {
			fail(e.toString());
		}
	}
}
