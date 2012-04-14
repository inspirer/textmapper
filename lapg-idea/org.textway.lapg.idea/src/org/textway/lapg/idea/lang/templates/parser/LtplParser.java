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
package org.textway.lapg.idea.lang.templates.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lang.templates.lexer.LtplElementType;
import org.textway.templates.ast.TemplatesLexer;
import org.textway.templates.ast.TemplatesLexer.ErrorReporter;
import org.textway.templates.ast.TemplatesLexer.LapgSymbol;
import org.textway.templates.ast.TemplatesParser;
import org.textway.templates.ast.TemplatesParser.ParseException;
import org.textway.templates.ast.TemplatesParser.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * evgeny, 3/3/12
 */
public class LtplParser implements PsiParser {

	private static final Map<Integer, IElementType> types = initTypes();

	private static Map<Integer, IElementType> initTypes() {
		Map<Integer, IElementType> result = new HashMap<Integer, IElementType>();
		result.put(Tokens.syntax_problem, TokenType.ERROR_ELEMENT);
		for (IElementType t : LtplElementTypes.allElements) {
			result.put(((LtplElementType) t).getSymbol(), t);
		}
		for (int expr : LtplElementTypes.allExpressions) {
			result.put(expr, LtplElementTypes.EXPRESSION);
		}
		return result;
	}

	private static IElementType reduceType(int token, int rule) {
		IElementType type = types.get(token);
		if (type != null) {
			return type;
		}

		return null;
	}

	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		parseBundle(builder);
		file.done(root);
		return builder.getTreeBuilt();
	}

	private void parseBundle(PsiBuilder builder) {
		Marker grammar = builder.mark();

		LtplParserEx parser = new LtplParserEx(builder);
		try {
			parser.parseInput(new LtplLexerEx(builder));
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
		grammar.done(LtplElementTypes.BUNDLE);
	}

	private static class LtplParserEx extends TemplatesParser {

		private final PsiBuilder myBuilder;
		private final Stack<Marker> markers = new Stack<Marker>();

		public LtplParserEx(PsiBuilder builder) {
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
		protected void shift() throws IOException {
			Marker marker = lapg_n.lexem != Tokens.eoi ? mark() : null;
			super.shift();
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
				IElementType elementType = reduceType(lapg_gg.lexem, rule);
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

	private static class LtplLexerEx extends TemplatesLexer {
		private final PsiBuilder myBuilder;
		private LapgSymbol next;

		public LtplLexerEx(PsiBuilder builder) throws IOException {
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
				next.lexem = Lexems.eoi;
			} else {
				LtplElementType tokenType = (LtplElementType) myBuilder.getTokenType();
				next.lexem = tokenType.getSymbol();
			}
			return next;
		}
	}
}
