/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.unicode;

import org.junit.Test;
import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.unicode.UnicodeData;
import org.textmapper.tool.bootstrap.unicode.UnicodeTestLexer.Span;
import org.textmapper.tool.bootstrap.unicode.UnicodeTestLexer.Tokens;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 2/21/12
 */
public class UnicodeTest {

	@Test
	public void testInts() {
		valid("12 23 123", Tokens.icon, Tokens.icon, Tokens.icon);
		valid("900000000", Tokens.icon);
		valid("\n\t 1 \t\n", Tokens.icon);
	}

	@Test
	public void testIds() {
		valid("_", Tokens.identifier);
		valid("a", Tokens.identifier);
		valid("aa12 23 zaAa_", Tokens.identifier, Tokens.icon, Tokens.identifier);
	}

	@Test
	public void testStrings() {
		valid("\"a\"", Tokens.string);
		CharacterSet Ll = UnicodeData.getInstance().getCharacterSet("Ll");
		for (int[] range : Ll) {
			for (int i = range[0]; i < range[1]; i++) {
				// TODO support > 0xffff
				if (i > 0xffff) return;
				assertTrue(Ll.contains(i));
				valid("\"" + Character.toString((char) i) + "\"", Tokens.string);
			}
		}
	}

	private void valid(String text, int... expectedTokens) {
		UnicodeTestLexer lexer;
		try {
			lexer = new UnicodeTestLexer(text, (message, line, offset) -> fail("unexpected failure: " + line + ": " + message));
			Span next;
			for (int expected : expectedTokens) {
				next = lexer.next();
				assertFalse(next.symbol == Tokens.eoi);
				assertEquals(expected, next.symbol);
			}
			next = lexer.next();
			assertTrue(next.symbol == Tokens.eoi);

		} catch (IOException e) {
			fail(e.toString());
		}
	}
}
