/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.LapgElementType;
import org.textmapper.idea.lang.syntax.lexer.LapgTemplatesElementType;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.ErrorReporter;
import org.textmapper.tool.parser.TMLexer.LapgSymbol;
import org.textmapper.tool.parser.TMParser;
import org.textmapper.tool.parser.TMParser.ParseException;
import org.textmapper.tool.parser.TMParser.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TMPsiParser implements PsiParser {

	private static final Map<Integer, IElementType> types = initTypes();

	private static Map<Integer, IElementType> initTypes() {
		Map<Integer, IElementType> result = new HashMap<Integer, IElementType>();
		for (IElementType t : TextmapperElementTypes.allElements) {
			int symbol = ((LapgElementType) t).getSymbol();
			if (symbol >= 0) {
				result.put(symbol, t);
			}
		}
		result.put(Tokens.syntax_problem, TokenType.ERROR_ELEMENT);
		return result;
	}

	private static IElementType reduceType(int token) {
		return types.get(token);
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

		TMParserEx parser = new TMParserEx(builder);
		try {
			parser.parseInput(new TMLexerEx(builder));
		} catch (IOException e) {
			/* cannot happen */
		} catch (ParseException e) {
			/* syntax error, ok */
		}

		boolean cannotRecover = !parser.markers.isEmpty();
		while (!parser.markers.isEmpty()) {
			parser.markers.pop().drop();
		}

		while (!builder.eof()) {
			builder.advanceLexer();
		}
		grammar.done(TextmapperElementTypes.GRAMMAR);
	}

	private static class TMParserEx extends TMParser {

		private final PsiBuilder myBuilder;
		private final Stack<Marker> markers = new Stack<Marker>();

		public TMParserEx(PsiBuilder builder) {
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
			if (sym.value != null) {
				Marker m = (Marker) sym.value;
				free(m, true);
				sym.value = null;
			}
		}

		@Override
		protected void shift() throws IOException {
			Marker marker = tmNext.symbol != Tokens.eoi ? mark() : null;
			super.shift();
			tmStack[tmHead].value = marker;
		}

		@Override
		protected void applyRule(LapgSymbol lapg_gg, int rule, int rulelen) {
			for (int i = 0; i < rulelen - 1; i++) {
				drop(tmStack[tmHead - i]);
			}
			if (rulelen > 0) {
				tmStack[tmHead - (rulelen - 1)].value = null;
			}

			Marker m = (Marker) lapg_gg.value;
			if (m != null) {
				IElementType elementType = reduceType(lapg_gg.symbol);
				if (elementType != null) {
					lapg_gg.value = clone(m);

					if (lapg_gg.symbol == Tokens.syntax_problem) {
						m.error("syntax error");
					} else {
						m.done(elementType);
					}
				}
			}
			if (lapg_gg.symbol == Tokens.input) {
				drop(lapg_gg);
			}
		}

		@Override
		protected boolean restore() {
			boolean restored = super.restore();
			if (restored) {
				/* restored after syntax error - mark the location */
				tmStack[tmHead].value = mark();
			}
			return restored;
		}

		@Override
		protected void dispose(LapgSymbol sym) {
			drop(sym);
		}

		@Override
		protected void cleanup(LapgSymbol sym) {
			assert sym.value == null;
		}
	}

	private static class TMLexerEx extends TMLexer {
		private final PsiBuilder myBuilder;
		private LapgSymbol next;

		public TMLexerEx(PsiBuilder builder) throws IOException {
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
				while (!myBuilder.eof() && myBuilder.getTokenType() == TokenType.BAD_CHARACTER) {
					myBuilder.advanceLexer();
				}
			}
			next = new LapgSymbol();
			if (myBuilder.eof()) {
				next.symbol = Lexems.eoi;
			} else if (myBuilder.getTokenType() instanceof LapgTemplatesElementType) {
				LapgTemplatesElementType tokenType = (LapgTemplatesElementType) myBuilder.getTokenType();
				next.symbol = tokenType.getSymbol();
			} else {
				LapgElementType tokenType = (LapgElementType) myBuilder.getTokenType();
				next.symbol = tokenType.getSymbol();
				if (next.symbol == Tokens.command) {
					// temp hack
					return nextInternal();
				}
			}
			return next;
		}
	}
}
