/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lexer.LapgElementType;
import org.textway.lapg.parser.LapgLexer;
import org.textway.lapg.parser.LapgLexer.ErrorReporter;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;
import org.textway.lapg.parser.LapgParser.ParseException;
import org.textway.lapg.parser.LapgParser.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LapgParser implements PsiParser {

	private static final Map<Integer, IElementType> types = initTypes();

	private static Map<Integer, IElementType> initTypes() {
		Map<Integer, IElementType> result = new HashMap<Integer, IElementType>();
		for (IElementType t : LapgElementTypes.allElements) {
			result.put(((LapgElementType) t).getSymbol(), t);
		}
		result.put(Tokens.syntax_problem, TokenType.ERROR_ELEMENT);
		return result;
	}

	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		parseGrammar(builder);
		file.done(root);
		return builder.getTreeBuilt();
	}

	private void parseGrammar(PsiBuilder builder) {
		Marker grammar = builder.mark();

		LapgParserEx parser = new LapgParserEx(builder);
		try {
			parser.parseInput(new LapgLexerEx(builder));
		} catch (IOException e) {
			/* cannot happen */
		} catch (ParseException e) {
			/* syntax error, ok */
		}

		assert parser.markers.isEmpty();

		while (!builder.eof()) {
			builder.advanceLexer();
		}
		grammar.done(LapgElementTypes.GRAMMAR);
	}

	private static class LapgParserEx extends org.textway.lapg.parser.LapgParser {

		private final PsiBuilder myBuilder;
		private final Stack<Marker> markers = new Stack<Marker>();

		public LapgParserEx(PsiBuilder builder) {
			super(new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
					// ignore, errors are reported as syntax_problem productions
				}
			});
			myBuilder = builder;
		}

		private Marker mark() {
			Marker m = myBuilder.mark();
			markers.push(m);
			return m;
		}

		private Marker clone(Marker inner) {
			Marker outer = inner.precede();
			free(inner, false);
			markers.push(outer);
			return outer;
		}

		private void free(Marker m, boolean drop) {
			assert m == markers.pop();
			if (drop) {
				m.drop();
			}
		}

		private void drop(LapgSymbol sym) {
			if (sym.sym != null) {
				Marker m = (Marker) sym.sym;
				free(m, true);
				sym.sym = null;
			}
		}

		@Override
		protected void shift(LapgLexer lexer) throws IOException {
			Marker marker = lapg_n.lexem != Tokens.eoi ? mark() : null;
			super.shift(lexer);
			lapg_m[lapg_head].sym = marker;
		}

		@Override
		protected void applyRule(LapgSymbol lapg_gg, int rule, int rulelen) {
			for (int i = 0; i < rulelen - 1; i++) {
				drop(lapg_m[lapg_head - i]);
			}
			if (rulelen > 0) {
				lapg_m[lapg_head - (rulelen - 1)].sym = null;
			}

			Marker m = (Marker) lapg_gg.sym;
			if (m != null) {
				IElementType elementType = types.get(lapg_gg.lexem);
				if (elementType != null) {
					lapg_gg.sym = clone(m);

					if (lapg_gg.lexem == Tokens.syntax_problem) {
						m.error("syntax error");
					} else {
						m.done(elementType);
					}
				}
			}
			if (lapg_gg.lexem == Tokens.input) {
				drop(lapg_gg);
			}
		}

		@Override
		protected boolean restore() {
			boolean restored = super.restore();
			if (restored) {
				/* restored after syntax error - mark the location */
				lapg_m[lapg_head].sym = mark();
			}
			return restored;
		}

		@Override
		protected void dispose(LapgSymbol sym) {
			drop(sym);
		}

		@Override
		protected void cleanup(LapgSymbol sym) {
			assert sym.sym == null;
		}
	}

	private static class LapgLexerEx extends LapgLexer {
		private final PsiBuilder myBuilder;
		private LapgSymbol next;

		public LapgLexerEx(PsiBuilder builder) throws IOException {
			super(null, null);
			myBuilder = builder;
		}

		@Override
		public LapgSymbol next() throws IOException {
			return nextInternal();
		}

		@Override
		public void reset(Reader stream) throws IOException {
		}

		private LapgSymbol nextInternal() {
			if (next != null && !myBuilder.eof()) {
				myBuilder.advanceLexer();
				while(!myBuilder.eof() && myBuilder.getTokenType() == TokenType.BAD_CHARACTER) {
					myBuilder.advanceLexer();
				}
			}
			next = new LapgSymbol();
			if (myBuilder.eof()) {
				next.lexem = Lexems.eoi;
			} else {
				LapgElementType tokenType = (LapgElementType) myBuilder.getTokenType();
				next.lexem = tokenType.getSymbol();
				if (next.lexem == Tokens.command) {
					// temp hack
					return nextInternal();
				}
			}
			return next;
		}
	}
}
