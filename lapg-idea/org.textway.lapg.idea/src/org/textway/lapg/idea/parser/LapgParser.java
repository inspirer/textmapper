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
import org.textway.lapg.idea.lexer.LapgTokenTypes;
import org.textway.lapg.parser.LapgLexer;
import org.textway.lapg.parser.LapgLexer.ErrorReporter;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;
import org.textway.lapg.parser.LapgLexer.Lexems;
import org.textway.lapg.parser.LapgParser.ParseException;
import org.textway.lapg.parser.LapgParser.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class LapgParser implements PsiParser {

	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		new Parser(builder).parseGrammar();
		file.done(root);
		return builder.getTreeBuilt();
	}

	private static class Parser implements LapgTokenTypes, LapgElementTypes {

		private final PsiBuilder myBuilder;
		private LapgSymbol next;
		private Stack<Marker> markers = new Stack<Marker>();
		private Map<Integer, IElementType> types = new HashMap<Integer, IElementType>();

		public Parser(PsiBuilder myBuilder) {
			this.myBuilder = myBuilder;
			for (IElementType t : LapgElementTypes.allElements) {
				types.put(((LapgElementType) t).getSymbol(), t);
			}
			types.put(Tokens.syntax_problem, TokenType.ERROR_ELEMENT);
		}

		private Marker mark() {
			Marker m = myBuilder.mark();
			markers.add(m);
			return m;
		}

		private void free(Marker m, boolean drop) {
			assert m == markers.pop();
			if(drop) {
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

		private LapgSymbol next() {
			if (next != null && !myBuilder.eof()) {
				myBuilder.advanceLexer();
			}
			next = new LapgSymbol();
			if (myBuilder.eof()) {
				next.lexem = Lexems.eoi;
			} else {
				LapgElementType tokenType = (LapgElementType) myBuilder.getTokenType();
				next.lexem = tokenType.getSymbol();
				if (next.lexem == Tokens.command) {
					drop(next);
					// temp hack
					return next();
				}
			}
			return next;
		}

		public void parseGrammar() {
			Marker grammar = myBuilder.mark();

			LapgParserEx parser = new LapgParserEx(new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
					// ignore
				}
			});
			try {
				parser.parseInput(new LapgLexerEx());
			} catch (IOException e) {
			} catch (ParseException e) {
			}

			assert markers.isEmpty();

			while (!myBuilder.eof()) {
				next();
			}
			grammar.done(GRAMMAR);
		}

		private class LapgLexerEx extends LapgLexer {
			public LapgLexerEx() throws IOException {
				super(null, null);
			}

			@Override
			public LapgSymbol next() throws IOException {
				return Parser.this.next();
			}

			@Override
			public void reset(Reader stream) throws IOException {
			}
		}

		private class LapgParserEx extends org.textway.lapg.parser.LapgParser {

			public LapgParserEx(ErrorReporter reporter) {
				super(reporter);
			}

			@Override
			protected void shift(LapgLexer lexer) throws IOException {
				if(lapg_n.lexem != Tokens.eoi) {
					Marker marker = mark();
					super.shift(lexer);
					lapg_m[lapg_head].sym = marker;
				} else {
					super.shift(lexer);
				}
			}

			@Override
			protected void applyRule(LapgSymbol lapg_gg, int rule, int rulelen) {
				for(int i = 0; i < rulelen - 1; i++) {
					Parser.this.drop(lapg_m[lapg_head - i]);
				}
				if(rulelen > 0) {
					lapg_m[lapg_head - (rulelen - 1)].sym = null;
				}

				Marker m = (Marker) lapg_gg.sym;
				if (m != null) {
					IElementType elementType = types.get(lapg_gg.lexem);
					if (elementType != null) {
						Marker outer = m.precede();
						free(m, false);
						markers.push(outer);
						lapg_gg.sym = outer;

//						System.out.println("reducing " + lapg_syms[lapg_gg.lexem] + " " + m + " before " + lapg_syms[next.lexem]);
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
					lapg_m[lapg_head].sym = mark();
				}
				return restored;
			}

			@Override
			protected void dispose(LapgSymbol sym) {
				Parser.this.drop(sym);
			}

			@Override
			protected void cleanup(LapgSymbol sym) {
				assert sym.sym == null;
			}
		}
	}
}
