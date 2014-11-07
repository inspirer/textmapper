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
package org.textmapper.tool.test.bootstrap.eoi;

import org.junit.Test;
import org.textmapper.tool.test.bootstrap.eoi.EoiLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.eoi.EoiLexer.Tokens;
import org.textmapper.tool.test.bootstrap.eoi.EoiTree.TextSource;

import java.io.CharArrayReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EoiTest {

	@Test
	public void testParser() {
		check("(;)");
		check("(;");

		check("(ii:(;);)");
		check("(ii:(;);");
		check("(ii:(;;)");
		check("(ii:(;;");

		check("(ii:(ee:(;););)");
		check("(ii:(ee:(;;;");

		check("(ii:(ee:(;), cc:(dd:(;);););)");
		check("(ii:(ee:(;, cc:(dd:(;;;;");
	}

	@Test
	public void testLexer() throws IOException {
		checkTokens("(;)", Tokens.Lparen, Tokens.Semicolon, Tokens.Rparen,
				Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI);
		checkTokens("(;", Tokens.Lparen, Tokens.Semicolon, Tokens._retfromA,
				Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI);

		checkTokens("(ii:(;);)",
				Tokens.Lparen, Tokens.id, Tokens.Colon, Tokens.Lparen, Tokens.Semicolon,
				Tokens.Rparen, Tokens.Semicolon, Tokens.Rparen,
				Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI);

		checkTokens("(ii:(;;",
				Tokens.Lparen, Tokens.id, Tokens.Colon, Tokens.Lparen,
				Tokens.Semicolon, Tokens.Semicolon,
				Tokens._retfromB, Tokens._retfromA,
				Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI);

		checkTokens("(ii:(ee:(;;;",
				Tokens.Lparen, Tokens.id, Tokens.Colon, Tokens.Lparen, Tokens.id, Tokens.Colon, Tokens.Lparen,
				Tokens.Semicolon, Tokens.Semicolon, Tokens.Semicolon,
				Tokens._retfromB, Tokens._retfromA,
				Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI, Tokens._customEOI);

		checkTokens("<c>", Tokens.gotoc);
	}

	private void check(String s) {
		EoiTree<Object> tree = EoiTree.parse(new TextSource("input", s.toCharArray(), 1));
		if (!tree.getErrors().isEmpty()) {
			fail(tree.getErrors().toString());
		}
	}

	private void checkTokens(String s, int... types) throws IOException {
		EoiLexer lexer = new EoiLexer(new CharArrayReader(s.toCharArray()), new ErrorReporter() {
			@Override
			public void error(String message, int line, int offset, int endoffset) {
				fail(line + ":" + message);
			}
		}) {
			@Override
			protected boolean createToken(Span token, int ruleIndex) throws IOException {
				super.createToken(token, ruleIndex);
				// Return (space) tokens as well.
				return true;
			}
		};
		int next;
		int index = 0;
		while ((next = lexer.next().symbol) != Tokens.eoi) {
			assertTrue("unexpected " + next, index < types.length);
			assertEquals(types[index++], next);
		}
		assertEquals(types.length, index);
	}

}
